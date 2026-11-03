package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_days",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["planId"])]
)
data class WorkoutDayEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val dayOfWeek: String,
    val title: String,
    val isCompleted: Boolean = false,
    val isRestDay: Boolean = false,
    val orderIndex: Int = 0
)
