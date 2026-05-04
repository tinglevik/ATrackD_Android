package com.example.actitracker.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.actitracker.data.*
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.data.model.GoalItem
import com.example.actitracker.data.model.TagItem
import com.example.actitracker.data.repository.ActivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TodayViewModel(
    private val repository: ActivityRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    private val _activeActivityId = MutableStateFlow<Long?>(null)
    val activeActivityId: StateFlow<Long?> = _activeActivityId

    private val _activeStartTime = MutableStateFlow<Long?>(null)
    val activeStartTime: StateFlow<Long?> = _activeStartTime

    private val _ticker = MutableStateFlow(System.currentTimeMillis())
    val ticker: StateFlow<Long> = _ticker

    private val _startOfToday = MutableStateFlow(getStartOfToday())

    private val _firstStartTimes = MutableStateFlow<Map<Long, Long>>(emptyMap())

    val activities: StateFlow<List<ActivityItem>> =
        repository.getActivities().map { entities ->
            entities.map { it.toItem() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // We need a more complex activities flow that includes elapsed time
    private val _activitiesWithStats = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activitiesWithStats: StateFlow<List<ActivityItem>> = _activitiesWithStats

    val tags: StateFlow<List<TagItem>> =
        repository.getAllTags().map { entities ->
            entities.map { TagItem(it.id, it.name, Color(it.color.toULong()), it.sortOrder) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalItem>> =
        repository.getAllGoals().map { entities ->
            entities.map { GoalItem(it.id, it.name, it.targetSeconds, it.period) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.sanitizeTimestamps()
        }
        startTicker()
        observeActiveSession()
        observeFirstStartTimes()
        observeActivitiesWithSessions()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            repository.getActiveSessionFlow().collect { session ->
                _activeActivityId.value = session?.activityId
                _activeStartTime.value = session?.startTime
            }
        }
    }

    private fun observeFirstStartTimes() {
        viewModelScope.launch {
            combine(
                settingsDataStore.firstStartTimesFlow,
                _startOfToday
            ) { savedStarts, today ->
                savedStarts.filter { it.value >= today }
            }.collect { filtered ->
                val today = _startOfToday.value
                val current = _firstStartTimes.value
                // Combine data from the storage with locally set ones (e.g., midnight for the active task)
                // to avoid flickering or data loss when transitioning between days
                _firstStartTimes.value = filtered + current.filter { it.value >= today }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeActivitiesWithSessions() {
        viewModelScope.launch {
            _startOfToday.flatMapLatest { startOfToday ->
                combine(
                    repository.getActivities(),
                    repository.getSessionsFromFlow(startOfToday),
                    _firstStartTimes,
                    _ticker,
                    _activeActivityId,
                    _activeStartTime
                ) { flows ->
                    @Suppress("UNCHECKED_CAST")
                    val entities = flows[0] as List<ActivityEntity>
                    @Suppress("UNCHECKED_CAST")
                    val todaySessions = flows[1] as List<ActivityLogEntity>
                    @Suppress("UNCHECKED_CAST")
                    val firstStarts = flows[2] as Map<Long, Long>
                    val ticker = flows[3] as Long
                    val activeId = flows[4] as Long?
                    val activeStartTime = flows[5] as Long?

                    entities.map { entity ->
                        val sessions = todaySessions.filter { it.activityId == entity.id }
                        // Суммируем только ЗАВЕРШЕННЫЕ сегодня части сессий
                        var totalSecondsToday = sessions.sumOf { session ->
                            val endTime = session.endTime ?: return@sumOf 0L 
                            val start = maxOf(session.startTime, startOfToday)
                            // Дополнительная защита: если данные еще не санированы, ограничиваем тикером
                            val effectiveEnd = minOf(endTime, ticker)
                            if (effectiveEnd > start) (effectiveEnd - start) / 1000 else 0L
                        }

                        // Добавляем время только для ОФИЦИАЛЬНО активной задачи
                        if (entity.id == activeId && activeStartTime != null) {
                            val effectiveStart = maxOf(activeStartTime, startOfToday)
                            // Здесь ticker уместен, так как задача реально запущена
                            if (ticker > effectiveStart) {
                                totalSecondsToday += (ticker - effectiveStart) / 1000
                            }
                        }

                        // Determine the start timestamp for displaying "Started at"
                        var firstStart = firstStarts[entity.id]
                        if (entity.id == activeId && activeStartTime != null && activeStartTime < startOfToday) {
                            // If the task carried over from the previous day, it "started" at midnight for the current day
                            firstStart = startOfToday
                        }

                        ActivityItem(
                            id = entity.id,
                            name = entity.name,
                            color = Color(entity.color.toULong()),
                            icon = entity.icon,
                            elapsedSeconds = totalSecondsToday,
                            history = emptyMap(),
                            firstStartDayTime = firstStart,
                            showInQuickPanel = entity.showInQuickPanel,
                            tagIds = entity.tagIds,
                            sortOrder = entity.sortOrder
                        )
                    }
                }
            }.collect { items ->
                _activitiesWithStats.value = items
            }
        }
    }

    private fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startTicker() {
        viewModelScope.launch {
            tickerFlow(1000).collect { now ->
                _ticker.value = now
                val currentStart = getStartOfToday()
                if (currentStart != _startOfToday.value) {
                    val activeId = _activeActivityId.value
                    val currentStarts = _firstStartTimes.value
                    val newStarts = currentStarts.filter { it.value >= currentStart }.toMutableMap()
                    
                    if (activeId != null) {
                        newStarts[activeId] = currentStart
                    }

                    _startOfToday.value = currentStart
                    _firstStartTimes.value = newStarts
                    
                    // Save to storage asynchronously
                    viewModelScope.launch {
                        updateFirstStartTimes(newStarts)
                    }
                }
            }
        }
    }

    private suspend fun updateFirstStartTimes(newTimes: Map<Long, Long>) {
        settingsDataStore.saveFirstStartTimes(newTimes)
    }

    fun addActivity(activity: ActivityItem) {
        viewModelScope.launch {
            val maxSortOrder = _activitiesWithStats.value.maxOfOrNull { it.sortOrder } ?: 0
            repository.insertActivityAndGetId(
                ActivityEntity(
                    id = 0,
                    name = activity.name,
                    color = activity.color.value.toLong(),
                    icon = activity.icon,
                    showInQuickPanel = activity.showInQuickPanel,
                    tagIds = activity.tagIds,
                    sortOrder = maxSortOrder + 1
                )
            )
        }
    }

    fun startActivity(activityId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.closeAllActiveSessionsExcept(activityId)
            repository.startActivitySession(activityId)

            val currentFirstStarts = _firstStartTimes.value
            if (currentFirstStarts[activityId] == null ||
                !isSameDay(currentFirstStarts[activityId]!!, now)
            ) {
                updateFirstStartTimes(currentFirstStarts + (activityId to now))
            }
        }
    }

    fun stopActivity(activityId: Long) {
        viewModelScope.launch {
            repository.stopActivitySession(activityId)
        }
    }

    fun updateActivity(updated: ActivityItem) {
        viewModelScope.launch {
            repository.updateActivity(
                ActivityEntity(
                    id = updated.id,
                    name = updated.name,
                    color = updated.color.value.toLong(),
                    icon = updated.icon,
                    showInQuickPanel = updated.showInQuickPanel,
                    tagIds = updated.tagIds,
                    sortOrder = updated.sortOrder
                )
            )
        }
    }

    fun reorderActivities(reorderedList: List<ActivityItem>) {
        viewModelScope.launch {
            val updatedEntities = reorderedList.mapIndexed { index, item ->
                ActivityEntity(
                    id = item.id,
                    name = item.name,
                    color = item.color.value.toLong(),
                    icon = item.icon,
                    showInQuickPanel = item.showInQuickPanel,
                    tagIds = item.tagIds,
                    sortOrder = index
                )
            }
            repository.updateActivities(updatedEntities)
        }
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            repository.stopActivitySession(activityId)
            repository.deleteActivity(activityId)
        }
    }

    // Tags actions
    fun addTag(tag: TagItem) {
        viewModelScope.launch {
            val maxSortOrder = tags.value.maxOfOrNull { it.sortOrder } ?: 0
            repository.insertTag(
                TagEntity(
                    name = tag.name,
                    color = tag.color.value.toLong(),
                    sortOrder = maxSortOrder + 1
                )
            )
        }
    }

    fun updateTag(tag: TagItem) {
        viewModelScope.launch {
            repository.updateTag(
                TagEntity(
                    id = tag.id,
                    name = tag.name,
                    color = tag.color.value.toLong(),
                    sortOrder = tag.sortOrder
                )
            )
        }
    }

    fun reorderTags(reorderedList: List<TagItem>) {
        viewModelScope.launch {
            val updatedEntities = reorderedList.mapIndexed { index, item ->
                TagEntity(
                    id = item.id,
                    name = item.name,
                    color = item.color.value.toLong(),
                    sortOrder = index
                )
            }
            repository.updateTags(updatedEntities)
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            repository.deleteTag(tagId)
        }
    }

    // Goals actions
    fun addGoal(goal: GoalItem) {
        viewModelScope.launch {
            repository.insertGoal(GoalEntity(name = goal.name, targetSeconds = goal.targetSeconds, period = goal.period))
        }
    }

    fun updateGoal(goal: GoalItem) {
        viewModelScope.launch {
            repository.updateGoal(GoalEntity(id = goal.id, name = goal.name, targetSeconds = goal.targetSeconds, period = goal.period))
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun startCreating() { _isCreating.value = true }
    fun stopCreating() { _isCreating.value = false }

    fun toggleQuickPanel(activity: ActivityItem) {
        viewModelScope.launch {
            repository.setShowInQuickPanel(activity.id, activity.showInQuickPanel)
        }
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun ActivityEntity.toItem() = ActivityItem(
    id = id,
    name = name,
    color = Color(color.toULong()),
    icon = icon,
    showInQuickPanel = showInQuickPanel,
    tagIds = tagIds,
    sortOrder = sortOrder
)
