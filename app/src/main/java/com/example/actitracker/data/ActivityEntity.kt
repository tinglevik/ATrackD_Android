package com.example.actitracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "activities")
@TypeConverters(ActivityConverters::class)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long,
    val icon: String,
    val showInQuickPanel: Boolean = false,
    val tagIds: List<Long> = emptyList(),
    val sortOrder: Int = 0
)