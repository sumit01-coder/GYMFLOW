package com.gymflow.core.database

import androidx.room.TypeConverter
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.Role
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromRole(role: Role?): String? = role?.name

    @TypeConverter
    fun toRole(value: String?): Role? = value?.let {
        try {
            Role.valueOf(it)
        } catch (_: Exception) {
            Role.MEMBER
        }
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? = status?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? = value?.let {
        try {
            SyncStatus.valueOf(it)
        } catch (_: Exception) {
            SyncStatus.PENDING
        }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        return json.encodeToString(list)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromExerciseSetList(sets: List<ExerciseSet>?): String? {
        if (sets == null) return null
        return json.encodeToString(sets)
    }

    @TypeConverter
    fun toExerciseSetList(value: String?): List<ExerciseSet>? {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<ExerciseSet>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }
}
