package com.example.actitracker.data

import androidx.room.TypeConverter
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivityConverters {
    @TypeConverter
    fun fromColorLong(value: Long): Color = Color(value.toULong())

    @TypeConverter
    fun toColorLong(color: Color): Long = color.value.toLong()

    @TypeConverter
    fun fromStringToList(value: String?): List<Long> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromListToString(list: List<Long>?): String {
        return Gson().toJson(list ?: emptyList<Long>())
    }
}