package com.example.actitracker

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.actitracker.data.AppDatabase
import com.example.actitracker.data.SettingsDataStore
import com.example.actitracker.data.repository.ActivityRepository
import com.example.actitracker.service.ActivityTrackerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ActiTrackerApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "actitracker_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            .build()
    }

    val repository by lazy {
        ActivityRepository(database.activityDao())
    }

    val settingsDataStore by lazy {
        SettingsDataStore(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Наблюдаем за списком активностей для быстрой панели.
        // Если сервис остановлен, но в списке что-то появилось — запускаем его.
        applicationScope.launch {
            repository.getQuickPanelActivities().collect { activities ->
                if (activities.isNotEmpty()) {
                    val intent = Intent(this@ActiTrackerApplication, ActivityTrackerService::class.java)
                    try {
                        ContextCompat.startForegroundService(this@ActiTrackerApplication, intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
