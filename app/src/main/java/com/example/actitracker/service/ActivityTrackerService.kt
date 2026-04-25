package com.example.actitracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.actitracker.ActiTrackerApplication
import com.example.actitracker.MainActivity
import com.example.actitracker.R
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.receiver.ActivityActionReceiver
import com.example.actitracker.ui.components.IconMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ActivityTrackerService : Service() {

    companion object {
        const val ACTION_START = "com.example.actitracker.START"
        const val ACTION_STOP = "com.example.actitracker.STOP"
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "activity_tracker_channel"
        private const val MAX_NOTIFICATION_ACTIVITIES = 10
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentActivities: List<ActivityItem> = emptyList()
    private var activeActivityId: Long? = null
    
    private var backgroundColor: Int = Color.WHITE
    private var contentColor: Int = Color.BLACK

    private lateinit var app: ActiTrackerApplication
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        app = application as ActiTrackerApplication
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, 
                buildSimpleNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildSimpleNotification())
        }

        observeState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
                if (activityId != -1L) handleStart(activityId)
            }
            ACTION_STOP -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1L)
                if (activityId != -1L) handleStop(activityId)
            }
        }
        return START_STICKY
    }

    private fun observeState() {
        serviceScope.launch {
            combine(
                app.repository.getQuickPanelActivities(),
                app.repository.getActiveSessionFlow(),
                app.settingsDataStore.backgroundColorFlow,
                app.settingsDataStore.contentColorFlow
            ) { quickPanelEntities, activeSession, bg, content ->
                activeActivityId = activeSession?.activityId
                backgroundColor = bg
                contentColor = content

                quickPanelEntities
                    .take(MAX_NOTIFICATION_ACTIVITIES)
                    .map { entity ->
                        ActivityItem(
                            id = entity.id,
                            name = entity.name,
                            color = androidx.compose.ui.graphics.Color(entity.color),
                            icon = entity.icon,
                            showInQuickPanel = true
                        )
                    }
            }.collect { items ->
                currentActivities = items
                
                if (items.isEmpty()) {
                    stopForegroundService()
                } else {
                    updateNotification()
                }
            }
        }
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleStart(activityId: Long) {
        serviceScope.launch {
            app.repository.closeAllActiveSessionsExcept(activityId)
            app.repository.startActivitySession(activityId)
            
            val now = System.currentTimeMillis()
            val currentStarts = app.settingsDataStore.firstStartTimesFlow.first().toMutableMap()
            
            if (currentStarts[activityId] == null || !isSameDay(currentStarts[activityId]!!, now)) {
                currentStarts[activityId] = now
                app.settingsDataStore.saveFirstStartTimes(currentStarts)
            }
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun handleStop(activityId: Long) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                app.repository.stopActivitySession(activityId)
            } catch (e: Exception) {
                android.util.Log.e("ActivityTrackerService", "Error stopping activity $activityId", e)
            }
        }
    }

    private fun updateNotification() {
        try {
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSimpleNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today_outline)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun buildNotification(): Notification {
        val openAppIntent = buildOpenAppIntent()
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today_outline)
            .setContentIntent(openAppIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomContentView(buildRemoteViews())
            .setCustomBigContentView(buildRemoteViews())
            .build()
    }

    private fun buildOpenAppIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildRemoteViews(): RemoteViews {
        val views = RemoteViews(packageName, R.layout.notification_activity_list)
        
        val safeBg = if (backgroundColor == 0) Color.WHITE else backgroundColor
        val safeContent = if (contentColor == 0) Color.BLACK else contentColor

        views.setInt(R.id.notification_root, "setBackgroundColor", safeBg)
        views.setTextColor(R.id.notification_app_name, safeContent)
        views.setInt(R.id.notification_app_icon, "setColorFilter", safeContent)

        views.removeAllViews(R.id.notification_activities_container)

        currentActivities.forEachIndexed { index, activity ->
            val isActive = activity.id == activeActivityId
            val itemView = RemoteViews(packageName, R.layout.notification_activity_item)

            // Use Emoji from mapper for guaranteed visibility
            itemView.setTextViewText(R.id.activity_icon, IconMapper.getEmoji(activity.icon))
            
            itemView.setTextViewText(R.id.activity_name, activity.name)
            itemView.setTextColor(R.id.activity_name, safeContent)

            if (isActive) {
                itemView.setInt(R.id.activity_active_indicator, "setColorFilter", safeContent)
                itemView.setInt(R.id.activity_active_indicator, "setImageAlpha", 255)
            } else {
                itemView.setInt(R.id.activity_active_indicator, "setImageAlpha", 0)
            }

            itemView.setInt(R.id.activity_divider, "setColorFilter", safeContent)
            itemView.setInt(R.id.activity_divider, "setImageAlpha", 50)
            itemView.setViewVisibility(
                R.id.activity_divider,
                if (index < currentActivities.size - 1) View.VISIBLE else View.GONE
            )

            val toggleIntent = Intent(this, ActivityActionReceiver::class.java).apply {
                action = ActivityActionReceiver.ACTION_TOGGLE
                putExtra(ActivityActionReceiver.EXTRA_ACTIVITY_ID, activity.id)
                putExtra(ActivityActionReceiver.EXTRA_IS_ACTIVE, isActive)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                activity.id.toInt(),
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            itemView.setOnClickPendingIntent(R.id.activity_item_root, pendingIntent)

            views.addView(R.id.notification_activities_container, itemView)
        }

        return views
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
