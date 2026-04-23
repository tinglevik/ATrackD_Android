package com.example.actitracker.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "activity_tag_cross_ref",
    primaryKeys = ["activityId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class ActivityTagCrossRef(
    val activityId: Long,
    val tagId: Long
)
