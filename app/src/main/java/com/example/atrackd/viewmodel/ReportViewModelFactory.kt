package com.example.atrackd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.atrackd.data.model.ActivityItem
import com.example.atrackd.data.model.TagItem
import com.example.atrackd.data.repository.ActivityRepository
import kotlinx.coroutines.flow.StateFlow

class ReportViewModelFactory(
    private val activitiesFlow: StateFlow<List<ActivityItem>>,
    private val tagsFlow: StateFlow<List<TagItem>>,
    private val activeActivityIdFlow: StateFlow<Long?>,
    private val activeStartTimeFlow: StateFlow<Long?>,
    private val repository: ActivityRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(
                activitiesFlow,
                tagsFlow,
                activeActivityIdFlow,
                activeStartTimeFlow,
                repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}