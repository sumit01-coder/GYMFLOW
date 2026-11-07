package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outdoor_workouts")
data class OutdoorWorkoutEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val workoutType: String = "RUN", // "RUN", "WALK", "HIKE"
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val durationSeconds: Long = 0L,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Float = 0f,
    val avgPaceSecondsPerKm: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val caloriesBurned: Int = 0,
    val polylineEncoded: String = "",
    val isCompleted: Boolean = false,
    val isSynced: Boolean = false
)

