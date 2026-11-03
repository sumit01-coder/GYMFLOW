package com.gymflow.domain.calculator

/**
 * Pure domain calculator for estimated energy expenditure during workouts.
 *
 * Uses the standard MET (Metabolic Equivalent of Task) formula:
 * Calories (kcal) = MET * 3.5 * weightKg / 200.0 * durationMinutes
 */
object CalorieCalculator {

    enum class ExerciseIntensity {
        LIGHT,
        MODERATE,
        VIGOROUS
    }

    enum class WorkoutCategory(val defaultMet: Float) {
        RESISTANCE_LIGHT(3.5f),
        RESISTANCE_MODERATE(5.0f),
        RESISTANCE_VIGOROUS(6.0f),
        CARDIO(7.0f),
        HIIT(8.5f),
        CALISTHENICS(4.0f),
        STRETCHING_MOBILITY(2.5f),
        GENERAL(5.0f)
    }

    /**
     * Resolves the MET value based on exercise category and chosen intensity.
     */
    fun resolveMet(category: String, intensity: ExerciseIntensity = ExerciseIntensity.MODERATE): Float {
        val normalized = category.trim().lowercase()
        return when {
            normalized.contains("hiit") || normalized.contains("interval") -> 8.5f
            normalized.contains("cardio") || normalized.contains("run") || normalized.contains("row") ||
                normalized.contains("cycle") || normalized.contains("treadmill") -> 7.0f
            normalized.contains("stretch") || normalized.contains("mobility") || normalized.contains("yoga") -> 2.5f
            normalized.contains("chest") || normalized.contains("back") || normalized.contains("legs") ||
                normalized.contains("shoulders") || normalized.contains("arms") ||
                normalized.contains("resistance") || normalized.contains("weight") -> {
                when (intensity) {
                    ExerciseIntensity.LIGHT -> 3.5f
                    ExerciseIntensity.MODERATE -> 5.0f
                    ExerciseIntensity.VIGOROUS -> 6.0f
                }
            }
            else -> when (intensity) {
                ExerciseIntensity.LIGHT -> 3.5f
                ExerciseIntensity.MODERATE -> 5.0f
                ExerciseIntensity.VIGOROUS -> 6.5f
            }
        }
    }

    /**
     * Core MET calculation formula:
     * Calories (kcal) = MET * 3.5 * weightKg / 200.0 * durationMinutes
     */
    fun calculate(met: Float, weightKg: Float, durationMinutes: Float): Float {
        if (weightKg <= 0f || durationMinutes <= 0f || met <= 0f) return 0f
        return (met * 3.5f * weightKg / 200.0f) * durationMinutes
    }

    /**
     * Calculate calories burned given an exercise category, intensity, body weight, and duration in minutes.
     */
    fun calculateForCategory(
        category: String,
        weightKg: Float,
        durationMinutes: Float,
        intensity: ExerciseIntensity = ExerciseIntensity.MODERATE
    ): Float {
        val met = resolveMet(category, intensity)
        return calculate(met = met, weightKg = weightKg, durationMinutes = durationMinutes)
    }

    /**
     * Calculate calories burned given a workout duration in seconds.
     * Returns rounded whole calories.
     */
    fun calculateForSession(
        weightKg: Float,
        durationSeconds: Long,
        category: WorkoutCategory = WorkoutCategory.RESISTANCE_MODERATE
    ): Int {
        if (weightKg <= 0f || durationSeconds <= 0L) return 0
        val durationMinutes = durationSeconds / 60.0f
        val calories = calculate(met = category.defaultMet, weightKg = weightKg, durationMinutes = durationMinutes)
        return Math.round(calories)
    }
}
