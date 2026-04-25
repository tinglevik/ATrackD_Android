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

    suspend fun deleteActivity(id: Long) {
        dao.deleteActivityWithSessions(id)
    }

    // Tags
    fun getAllTags(): Flow<List<TagEntity>> = dao.getAllTags()

    suspend fun insertTag(tag: TagEntity): Long = dao.insertTag(tag)

    suspend fun updateTag(tag: TagEntity) = dao.updateTag(tag)

    suspend fun deleteTag(tagId: Long) = dao.deleteTag(tagId)

    fun getAllGoals(): Flow<List<GoalEntity>> = dao.getAllGoals()

    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)

    suspend fun deleteGoal(goalId: Long) = dao.deleteGoal(goalId)

    suspend fun startActivitySession(activityId: Long) {
        val session = ActivityLogEntity(
            activityId = activityId,
            startTime = System.currentTimeMillis(),
            endTime = null
        )
        dao.insertSession(session)
    }

    suspend fun stopActivitySession(activityId: Long) {
        val currentSession = dao.getActiveSession(activityId)
        currentSession?.let {
            val updated = it.copy(endTime = System.currentTimeMillis())
            dao.updateSession(updated)
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
