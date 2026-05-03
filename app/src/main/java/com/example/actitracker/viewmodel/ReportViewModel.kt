package com.example.actitracker.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.actitracker.data.ActivityLogEntity
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.data.model.TagItem
import com.example.actitracker.data.repository.ActivityRepository
import com.example.actitracker.ui.screens.ReportPeriod
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class ReportMode {
    ACTIVITIES, TAGS
}

data class ReportStats(
    val data: Map<String, Long>,
    val colors: Map<String, Color>,
    val totalSeconds: Long
)

// Helper class for storing "static" report data
private data class BaseReportData(
    val activities: List<ActivityItem>,
    val tags: List<TagItem>,
    val sessions: List<ActivityLogEntity>
)

class ReportViewModel(
    activitiesFlow: StateFlow<List<ActivityItem>>,
    tagsFlow: StateFlow<List<TagItem>>,
    private val activeActivityIdFlow: StateFlow<Long?>,
    private val activeStartTimeFlow: StateFlow<Long?>,
    private val repository: ActivityRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod

    private val _dateOffset = MutableStateFlow(0)
    val dateOffset: StateFlow<Int> = _dateOffset

    private val _reportMode = MutableStateFlow(ReportMode.ACTIVITIES)
    val reportMode: StateFlow<ReportMode> = _reportMode

    private val _statsData = MutableStateFlow(ReportStats(emptyMap(), emptyMap(), 0L))
    val statsData: StateFlow<ReportStats> = _statsData

    private val _ticker = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            tickerFlow(1000).collect { _ticker.value = it }
        }

        // Load data from the DB only when the period, offset, or activity list changes
        val baseDataFlow = combine(
            activitiesFlow,
            tagsFlow,
            _selectedPeriod,
            _dateOffset
        ) { activities, tags, period, offset ->
            val (from, to) = getDateRange(period, System.currentTimeMillis(), offset)
            val sessions = repository.getAllSessionsForPeriod(from, to)
            BaseReportData(activities, tags, sessions)
        }

        viewModelScope.launch {
            // Combine 7 flows. Using 'args' array since there's no overload for 7 flows.
            combine(
                baseDataFlow,
                activeActivityIdFlow,
                activeStartTimeFlow,
                _ticker,
                _reportMode,
                _selectedPeriod,
                _dateOffset
            ) { args ->
                val base = args[0] as BaseReportData
                val currentTime = args[3] as Long
                val mode = args[4] as ReportMode
                val period = args[5] as ReportPeriod
                val offset = args[6] as Int

                val (from, to) = getDateRange(period, currentTime, offset)
                
                computeStatsSync(
                    base.activities, base.tags, base.sessions, 
                    from, to,
                    currentTime, mode
                )
            }.collect { result ->
                _statsData.value = result
            }
        }
    }

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        _dateOffset.value = 0
    }

    fun toggleReportMode() {
        _reportMode.value = if (_reportMode.value == ReportMode.ACTIVITIES) ReportMode.TAGS else ReportMode.ACTIVITIES
    }

    fun nextDay() { _dateOffset.value += 1 }
    fun previousDay() { _dateOffset.value -= 1 }

    private fun computeStatsSync(
        activities: List<ActivityItem>,
        allTags: List<TagItem>,
        allSessions: List<ActivityLogEntity>,
        from: Long,
        to: Long,
        currentTime: Long,
        mode: ReportMode
    ): ReportStats {
        val data = mutableMapOf<String, Long>()
        val colors = mutableMapOf<String, Color>()
        var totalTrackedSeconds = 0L

        val sessionsByActivity = allSessions.groupBy { it.activityId }

        if (mode == ReportMode.ACTIVITIES) {
            for (activity in activities) {
                val sessions = sessionsByActivity[activity.id] ?: emptyList()
                val activitySeconds = sessions.sumOf { session ->
                    val start = maxOf(session.startTime, from)
                    val end = minOf(session.endTime ?: currentTime, to)
                    if (end > start) (end - start) / 1000 else 0L
                }

                if (activitySeconds > 0) {
                    data[activity.name] = activitySeconds
                    colors[activity.name] = activity.color
                    totalTrackedSeconds += activitySeconds
                }
            }
        } else {
            val tagStats = mutableMapOf<String, Long>()
            var noTagSeconds = 0L

            for (activity in activities) {
                val sessions = sessionsByActivity[activity.id] ?: emptyList()
                val activitySeconds = sessions.sumOf { session ->
                    val start = maxOf(session.startTime, from)
                    val end = minOf(session.endTime ?: currentTime, to)
                    if (end > start) (end - start) / 1000 else 0L
                }

                if (activitySeconds > 0) {
                    totalTrackedSeconds += activitySeconds
                    if (activity.tagIds.isEmpty()) {
                        noTagSeconds += activitySeconds
                    } else {
                        for (tagId in activity.tagIds) {
                            val tag = allTags.find { it.id == tagId }
                            val tagName = tag?.name ?: "Unknown Tag"
                            tagStats[tagName] = (tagStats[tagName] ?: 0L) + activitySeconds
                            if (tag != null) {
                                colors[tagName] = tag.color
                            }
                        }
                    }
                }
            }
            data.putAll(tagStats)
            if (noTagSeconds > 0) {
                data["No Tag"] = noTagSeconds
                colors["No Tag"] = Color.Gray
            }
        }

        return ReportStats(data, colors, totalTrackedSeconds)
    }

    private fun startOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getDateRange(period: ReportPeriod, now: Long, offset: Int): Pair<Long, Long> {
        val baseCalendar = Calendar.getInstance().apply { 
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, offset) 
        }
        val adjustedNow = baseCalendar.timeInMillis

        return when (period) {
            ReportPeriod.TODAY -> {
                val start = startOfDay(adjustedNow)
                start to (start + 24 * 3600 * 1000 - 1)
            }
            ReportPeriod.LAST_7_DAYS -> {
                val cal = Calendar.getInstance().apply { timeInMillis = adjustedNow }
                cal.add(Calendar.DAY_OF_YEAR, -6)
                startOfDay(cal.timeInMillis) to adjustedNow
            }
            ReportPeriod.LAST_30_DAYS -> {
                val cal = Calendar.getInstance().apply { timeInMillis = adjustedNow }
                cal.add(Calendar.DAY_OF_YEAR, -29)
                startOfDay(cal.timeInMillis) to adjustedNow
            }
            ReportPeriod.LAST_YEAR -> {
                val cal = Calendar.getInstance().apply { timeInMillis = adjustedNow }
                cal.add(Calendar.DAY_OF_YEAR, -364)
                startOfDay(cal.timeInMillis) to adjustedNow
            }
        }
    }
}
