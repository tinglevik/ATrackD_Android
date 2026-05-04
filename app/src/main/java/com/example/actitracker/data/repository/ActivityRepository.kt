package com.example.actitracker.data.repository

import com.example.actitracker.data.ActivityDao
import com.example.actitracker.data.ActivityEntity
import com.example.actitracker.data.ActivityLogEntity
import com.example.actitracker.data.GoalEntity
import com.example.actitracker.data.TagEntity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val dao: ActivityDao
) {

    fun getActivities(): Flow<List<ActivityEntity>> {
        return dao.getAllActivities()
    }

    suspend fun insertActivityAndGetId(activity: ActivityEntity): Long {
        return dao.insertActivityAndGetId(activity)
    }

    suspend fun updateActivity(activity: ActivityEntity) {
        dao.updateActivity(activity)
    }

    suspend fun updateActivities(activities: List<ActivityEntity>) {
        dao.updateActivities(activities)
    }

    suspend fun deleteActivity(id: Long) {
        dao.deleteActivityWithSessions(id)
    }

    // Tags
    fun getAllTags(): Flow<List<TagEntity>> = dao.getAllTags()

    suspend fun insertTag(tag: TagEntity): Long = dao.insertTag(tag)

    suspend fun updateTag(tag: TagEntity) = dao.updateTag(tag)

    suspend fun updateTags(tags: List<TagEntity>) = dao.updateTags(tags)

    suspend fun deleteTag(tagId: Long) = dao.deleteTag(tagId)

    fun getAllGoals(): Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)

    suspend fun deleteGoal(goalId: Long) = dao.deleteGoal(goalId)

    suspend fun startActivitySession(activityId: Long) {
        val now = System.currentTimeMillis()
        val session = ActivityLogEntity(
            activityId = activityId,
            startTime = now,
            endTime = null
        )
        dao.insertSession(session)
    }

    suspend fun stopActivitySession(activityId: Long) {
        val now = System.currentTimeMillis()
        val currentSession = dao.getActiveSession(activityId)
        currentSession?.let {
            // Protection: end time cannot be before start time
            val safeEndTime = maxOf(it.startTime, now)
            val updated = it.copy(endTime = safeEndTime)
            dao.updateSession(updated)
        }
    }

    suspend fun sanitizeTimestamps() {
        val now = System.currentTimeMillis()
        val sessions = dao.getAllSessions()
        sessions.forEach { session ->
            var changed = false
            var newStart = session.startTime
            var newEnd = session.endTime

            if (session.startTime > now) {
                newStart = now
                changed = true
            }
            if (session.endTime != null && session.endTime > now) {
                newEnd = now
                changed = true
            }

            if (changed) {
                dao.updateSession(session.copy(startTime = newStart, endTime = newEnd))
            }
        }
    }

    suspend fun getAllSessionsForPeriod(from: Long, to: Long): List<ActivityLogEntity> {
        return dao.getAllSessionsInInterval(from, to)
    }

    fun getSessionsFromFlow(from: Long): Flow<List<ActivityLogEntity>> {
        return dao.getSessionsFromFlow(from)
    }

    fun getActiveSessionFlow(): Flow<ActivityLogEntity?> {
        return dao.getActiveSessionFlow()
    }

    suspend fun closeAllActiveSessionsExcept(excludeActivityId: Long) {
        dao.closeAllActiveSessionsExcept(
            excludeActivityId = excludeActivityId,
            endTime = System.currentTimeMillis()
        )
    }

    fun getQuickPanelActivities(): Flow<List<ActivityEntity>> {
        return dao.getQuickPanelActivities()
    }

    suspend fun setShowInQuickPanel(id: Long, show: Boolean) {
        dao.setShowInQuickPanel(id, show)
    }
}
