package com.example.actitracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.actitracker.service.ActivityTrackerService

class ActivityActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TOGGLE = "com.example.actitracker.ACTION_TOGGLE"
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_IS_ACTIVE = "is_active"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // ✅ ДОБАВЛЕНО: логирование для диагностики
        android.util.Log.d(
            "ActivityActionReceiver",
            "onReceive: action=${intent.action}, " +
                    "activityId=${intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)}, " +
                    "isActive=${intent.getBooleanExtra(EXTRA_IS_ACTIVE, false)}"
        )

        if (intent.action == ACTION_TOGGLE) {
            val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
            val isActive = intent.getBooleanExtra(EXTRA_IS_ACTIVE, false)

            if (activityId != -1L) {
                val serviceIntent = Intent(context, ActivityTrackerService::class.java).apply {
                    // ✅ ИСПРАВЛЕНО: если активна — останавливаем, если нет — запускаем
                    action = if (isActive) ActivityTrackerService.ACTION_STOP
                    else ActivityTrackerService.ACTION_START
                    putExtra(ActivityTrackerService.EXTRA_ACTIVITY_ID, activityId)
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}