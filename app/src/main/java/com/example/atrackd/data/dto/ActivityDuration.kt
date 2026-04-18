package com.example.atrackd.data.dto

data class ActivityDuration(
    val activityId: Long,
    val day: String, // yyyy-MM-dd
    val duration: Long // миллисекунды
)