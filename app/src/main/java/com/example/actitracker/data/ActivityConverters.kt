package com.example.actitracker.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivityConverters {

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