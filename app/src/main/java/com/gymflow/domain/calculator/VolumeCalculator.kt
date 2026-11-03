package com.gymflow.domain.calculator

import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.WorkoutExercise

/**
 * Pure domain calculator for cumulative resistance training volume.
 * Volume = Weight (kg) * Repetitions
 */
object VolumeCalculator {

    /**
     * Calculates volume for a single set.
     */
    fun calculateSetVolume(weightKg: Float, reps: Int): Float {
        if (weightKg <= 0f || reps <= 0) return 0f
        return weightKg * reps
    }

    /**
     * Calculates cumulative volume from a list of completed [SetLog]s.
     */
    fun calculateTotalVolumeFromLogs(logs: List<SetLog>): Float {
        return logs.fold(0f) { acc, setLog ->
            acc + calculateSetVolume(setLog.actualWeightKg, setLog.actualReps)
        }
    }

    /**
     * Calculates cumulative volume from completed [ExerciseSet]s.
     */
    fun calculateExerciseVolume(sets: List<ExerciseSet>): Float {
        return sets.filter { it.isCompleted }.fold(0f) { acc, set ->
            val weight = set.actualWeightKg ?: set.targetWeightKg
            val reps = set.actualReps ?: set.targetReps
            acc + calculateSetVolume(weight, reps)
        }
    }

    /**
     * Calculates total volume for a list of workout exercises.
     */
    fun calculateWorkoutVolume(exercises: List<WorkoutExercise>): Float {
        return exercises.fold(0f) { acc, exercise ->
            acc + calculateExerciseVolume(exercise.sets)
        }
    }

    /**
     * Groups volume by exercise name from completed logs.
     */
    fun groupVolumeByExercise(logs: List<SetLog>): Map<String, Float> {
        return logs.groupBy { it.exerciseName }
            .mapValues { (_, exerciseLogs) ->
                calculateTotalVolumeFromLogs(exerciseLogs)
            }
    }

    /**
     * Calculates relative percentage difference between current volume and previous volume.
     */
    fun calculateVolumeDeltaPercent(currentVolume: Float, previousVolume: Float): Float {
        if (previousVolume <= 0f) return 0f
        return ((currentVolume - previousVolume) / previousVolume) * 100f
    }
}
