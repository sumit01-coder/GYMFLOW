package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outdoor_gps_points",
    indices = [Index(value = ["workoutId"])]
)
data class OutdoorGpsPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val workoutId: String,
    val timestampEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val cumulativeSteps: Int = 0
)

