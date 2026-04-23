package com.example.actitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetSeconds: Long,
    val period: String // e.g., "DAILY", "WEEKLY"
)
