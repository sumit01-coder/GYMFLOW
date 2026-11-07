package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey val id: String, // "steps_{memberId}_{date}"
    val memberId: String,
    val date: String, // "YYYY-MM-DD"
    val stepCount: Int,
    val targetGoal: Int = 10000,
    val distanceMeters: Float = 0f,
    val caloriesKcal: Int = 0,
    val activeMinutes: Int = 0,
    val lastSensorStepCount: Int = 0,
    val lastUpdatedEpochMs: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

