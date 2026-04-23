package com.example.actitracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.actitracker.data.SettingsDataStore
import com.example.actitracker.data.repository.ActivityRepository

class TodayViewModelFactory(
    private val repository: ActivityRepository,
    private val settingsDataStore: SettingsDataStore // ✅
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            TodayViewModel::class.java.isAssignableFrom(modelClass) ->
                TodayViewModel(repository, settingsDataStore) as T // ✅
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}