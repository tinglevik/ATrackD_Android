package com.example.actitracker.data.model

import androidx.compose.ui.graphics.Color

data class ActivityItem(
    val id: Long,
    val name: String,
    val color: Color,
    val icon: String,
    val elapsedSeconds: Long = 0, // accumulated time
    val history: Map<String, Long> = emptyMap(), // yyyy-MM-dd -> seconds
    val firstStartDayTime: Long? = null,
    val showInQuickPanel: Boolean = false,
    val tagIds: List<Long> = emptyList(),
    val sortOrder: Int = 0
)