package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymflow.domain.model.ExerciseSet

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dayId"]), Index(value = ["planId"])]
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val planId: String,
    val exerciseId: String,
    val exerciseName: String,
    val category: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val restDurationSeconds: Int = 45,
    val notes: String = "",
    val sets: List<ExerciseSet> = emptyList(),
    val orderIndex: Int = 0
)
