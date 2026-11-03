package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.Exercise
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // Chest, Back, Shoulders, Biceps, Triceps, Abs, Legs
    val muscleGroup: String,
    val equipment: String, // Barbell, Dumbbell, Cable, Machine, Bodyweight
    val machineName: String? = null,
    val defaultSets: Int = 3,
    val defaultReps: Int = 12,
    val defaultWeightKg: Float = 20f,
    val defaultRestSeconds: Int = 45,
    val mediaUrl: String? = null,
    val instructionsJson: String = "[]",
    val coachTip: String = ""
) {
    fun toDomain(): Exercise {
        val instructions = try {
            Json.decodeFromString<List<String>>(instructionsJson)
        } catch (_: Exception) {
            emptyList()
        }
        return Exercise(
            id = id,
            name = name,
            category = category,
            muscleGroup = muscleGroup,
            equipment = equipment,
            instructions = instructions,
            defaultRestSeconds = defaultRestSeconds,
            mediaUrl = mediaUrl,
            machineName = machineName,
            defaultSets = defaultSets,
            defaultReps = defaultReps,
            defaultWeightKg = defaultWeightKg,
            coachTip = coachTip
        )
    }

    companion object {
        fun fromDomain(exercise: Exercise): ExerciseEntity {
            val jsonStr = try {
                Json.encodeToString(exercise.instructions)
            } catch (_: Exception) {
                "[]"
            }
            return ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                category = exercise.category,
                muscleGroup = exercise.muscleGroup,
                equipment = exercise.equipment,
                machineName = exercise.machineName,
                defaultSets = exercise.defaultSets,
                defaultReps = exercise.defaultReps,
                defaultWeightKg = exercise.defaultWeightKg,
                defaultRestSeconds = exercise.defaultRestSeconds,
                mediaUrl = exercise.mediaUrl,
                instructionsJson = jsonStr,
                coachTip = exercise.coachTip
            )
        }
    }
}

