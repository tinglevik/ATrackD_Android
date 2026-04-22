package com.example.atrackd.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrackd.data.*
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.GoalItem
import com.example.atrackd.data.model.TagItem
import com.example.atrackd.data.repository.ActivityRepository
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
            entities.map { TagItem(it.id, it.name, Color(it.color.toULong())) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalItem>> =
        repository.getAllGoals().map { entities ->
            entities.map { GoalItem(it.id, it.name, it.targetSeconds, it.period) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        startTicker()
        observeActiveSession()
        observeFirstStartTimes()
        observeActivitiesWithSessions()
        resetFirstStartTimeAtDayEnd()
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
            val today = getStartOfToday()
            settingsDataStore.firstStartTimesFlow.collect { savedStarts ->
                _firstStartTimes.value = savedStarts.filter { (_, time) ->
                    time >= today
                }
            }
        }
    }

    private fun observeActivitiesWithSessions() {
        val startOfToday = getStartOfToday()

        viewModelScope.launch {
            combine(
                repository.getActivities(),
                repository.getSessionsFromFlow(startOfToday),
                _firstStartTimes
            ) { entities, todaySessions, firstStarts ->
                entities.map { entity ->
                    val sessions = todaySessions.filter { it.activityId == entity.id }
                    val completedSeconds = sessions.sumOf { session ->
                        session.endTime?.let { end -> (end - session.startTime) / 1000 } ?: 0L
                    }

                    ActivityItem(
                        id = entity.id,
                        name = entity.name,
                        color = Color(entity.color.toULong()),
                        icon = entity.icon,
                        elapsedSeconds = completedSeconds,
                        history = emptyMap(),
                        firstStartDayTime = firstStarts[entity.id],
                        showInQuickPanel = entity.showInQuickPanel,
                        tagIds = entity.tagIds
                    )
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

    private fun resetFirstStartTimeAtDayEnd() {
        viewModelScope.launch {
            tickerFlow(1000).collect { now ->
                val calendar = Calendar.getInstance().apply { timeInMillis = now }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                if (hour == 0 && minute == 0 && second < 2) {
                    val current = _firstStartTimes.value
                    if (current.isNotEmpty()) {
                        val filtered = current.filter { (_, time) -> isSameDay(time, now) }
                        updateFirstStartTimes(filtered)
                    }
                }
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            tickerFlow(1000).collect { _ticker.value = it }
        }
    }

    private suspend fun updateFirstStartTimes(newTimes: Map<Long, Long>) {
        settingsDataStore.saveFirstStartTimes(newTimes)
    }

    fun addActivity(activity: ActivityItem) {
        viewModelScope.launch {
            repository.insertActivityAndGetId(
                ActivityEntity(
                    id = 0,
                    name = activity.name,
                    color = activity.color.value.toLong(),
                    icon = activity.icon,
                    showInQuickPanel = activity.showInQuickPanel,
                    tagIds = activity.tagIds
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
            repository.closeAllActiveSessions()
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
                    tagIds = updated.tagIds
                )
            )
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
            repository.insertTag(TagEntity(name = tag.name, color = tag.color.value.toLong()))
        }
    }

    fun updateTag(tag: TagItem) {
        viewModelScope.launch {
            repository.updateTag(TagEntity(id = tag.id, name = tag.name, color = tag.color.value.toLong()))
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
    tagIds = tagIds
)
