package com.gymflow.domain.ai

import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import java.util.UUID

/**
 * Data model for individual AI coaching suggestions generated on-device.
 */
data class AiExerciseSuggestion(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val explanation: String,
    val recommendedSets: Int,
    val recommendedReps: Int,
    val targetRestSeconds: Int,
    val recommendedAlternative: String? = null,
    val scienceNote: String? = null,
    val tag: String = "OPTIMIZATION", // "VOLUME", "REST", "JOINT_HEALTH", "METABOLIC", "LOAD"
    val isApplied: Boolean = false
)

/**
 * Normalized user context for AI exercise recommendations.
 */
data class UserAiProfile(
    val age: Int = 28,
    val heightCm: Float = 175f,
    val weightKg: Float = 75f,
    val goal: String = "Hypertrophy",
    val experienceLevel: String = "Intermediate"
) {
    val bmi: Float
        get() = if (heightCm > 0f) {
            val heightM = heightCm / 100f
            weightKg / (heightM * heightM)
        } else 24.5f
}

/**
 * On-Device GYMFLOW AI Exercise Optimizer.
 * Executes purely local, science-based biomechanical and physiological heuristics
 * to optimize training volume, rep ranges, rest intervals, and joint health.
 */
object GymFlowAiOptimizer {

    /**
     * Main entry point: Generates actionable coaching recommendations based on
     * user biometrics, training goals, experience level, and planned exercises.
     */
    fun optimize(
        age: Int,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        experienceLevel: String = "Intermediate",
        exercises: List<WorkoutExercise> = emptyList()
    ): List<AiExerciseSuggestion> {
        val userProfile = UserAiProfile(
            age = if (age > 0) age else 28,
            heightCm = if (heightCm > 0f) heightCm else 175f,
            weightKg = if (weightKg > 0f) weightKg else 75f,
            goal = goal.ifBlank { "Hypertrophy" },
            experienceLevel = experienceLevel.ifBlank { "Intermediate" }
        )

        val suggestions = mutableListOf<AiExerciseSuggestion>()

        // 1. Goal-Specific Volume & Rest Heuristics
        suggestions.add(generateGoalSuggestion(userProfile))

        // 2. Age 40+ Joint-Friendly Adaptations
        val jointSuggestions = generateJointAdaptations(userProfile, exercises)
        suggestions.addAll(jointSuggestions)

        // 3. Experience Level & Biometric Fine-Tuning
        suggestions.add(generateExperienceSuggestion(userProfile))

        // 4. Exercise-Specific Micro-Optimizations (if specific routine is provided)
        exercises.take(2).forEach { ex ->
            val specific = generateExerciseSpecificSuggestion(ex, userProfile)
            if (specific != null && suggestions.none { it.title == specific.title }) {
                suggestions.add(specific)
            }
        }

        return suggestions
    }

    /**
     * Generates volume, rep, and rest recommendations tailored to the physiological goal.
     */
    private fun generateGoalSuggestion(profile: UserAiProfile): AiExerciseSuggestion {
        val normalizedGoal = profile.goal.lowercase()

        return when {
            // STRENGTH: 4-5 sets, 4-6 reps, heavier loads, 90-120s rest
            normalizedGoal.contains("strength") || normalizedGoal.contains("power") -> {
                AiExerciseSuggestion(
                    title = "Neuromuscular Strength Protocol",
                    explanation = "Structure work to 4-5 sets of 4-6 reps with heavier loads (>80% 1RM) and 90-120s rest to maximize central motor unit recruitment.",
                    recommendedSets = 5,
                    recommendedReps = 5,
                    targetRestSeconds = 120,
                    scienceNote = "High load training requires 90-120 seconds for near-complete phosphocreatine (ATP-CP) resynthesis between maximal efforts.",
                    tag = "VOLUME"
                )
            }
            // FAT LOSS / TONE: 3-4 sets, 12-15 reps, 45s rest
            normalizedGoal.contains("fat") || normalizedGoal.contains("loss") ||
                    normalizedGoal.contains("tone") || normalizedGoal.contains("cut") ||
                    normalizedGoal.contains("endurance") -> {
                AiExerciseSuggestion(
                    title = "Metabolic Conditioning & EPOC Protocol",
                    explanation = "Structure work to 3-4 sets of 12-15 reps with brisk 45s rest intervals to elevate metabolic burn and post-exercise oxygen consumption.",
                    recommendedSets = 3,
                    recommendedReps = 15,
                    targetRestSeconds = 45,
                    scienceNote = "Shorter rest (45s) maintains elevated lactate accumulation and continuous cardiovascular demand, increasing lipid oxidation during and after training.",
                    tag = "METABOLIC"
                )
            }
            // HYPERTROPHY (Default): 3-4 sets, 8-12 reps, 60s rest
            else -> {
                AiExerciseSuggestion(
                    title = "Hypertrophic Tension & Sarcoplasmic Volume",
                    explanation = "Target 3-4 sets of 8-12 reps with 60s rest to achieve the optimal balance of mechanical tension and intramuscular metabolic stress.",
                    recommendedSets = 4,
                    recommendedReps = 10,
                    targetRestSeconds = 60,
                    scienceNote = "The 8-12 rep continuum maximizes muscle protein synthesis pathways (mTORC1) while maintaining high time under tension.",
                    tag = "VOLUME"
                )
            }
        }
    }

    /**
     * Age 40+ Joint-Friendly Adaptations:
     * Detects barbell movements and recommends Dumbbell or Cable movements over heavy flat barbell.
     */
    private fun generateJointAdaptations(
        profile: UserAiProfile,
        exercises: List<WorkoutExercise>
    ): List<AiExerciseSuggestion> {
        val jointSuggestions = mutableListOf<AiExerciseSuggestion>()

        if (profile.age >= 40) {
            // General 40+ Joint Health Rule
            val hasHeavyBarbellBench = exercises.any {
                it.exerciseName.contains("bench", ignoreCase = true) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }
            val hasHeavySquat = exercises.any {
                it.exerciseName.contains("squat", ignoreCase = true) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }
            val hasOverheadPress = exercises.any {
                (it.exerciseName.contains("overhead", ignoreCase = true) ||
                        it.exerciseName.contains("military", ignoreCase = true)) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }

            if (hasHeavyBarbellBench) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        title = "Joint Protection: Converging Chest Movement",
                        explanation = "At age ${profile.age}, substitute Flat Barbell Bench with Incline Dumbbell Press or Cable Chest Fly to allow natural humeral rotation and protect rotator cuffs.",
                        recommendedSets = 4,
                        recommendedReps = 10,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Incline Dumbbell Press / Cable Fly",
                        scienceNote = "Fixed barbells enforce internal shoulder rotation at the bottom of the rep; independent dumbbells or cables preserve synovial joint integrity.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else if (hasHeavySquat) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        title = "Spinal Decompression: Unilateral Squats",
                        explanation = "Substitute heavy Barbell Back Squats with Bulgarian Split Squats or Hack Squats to dramatically reduce lumbar axial compression.",
                        recommendedSets = 3,
                        recommendedReps = 10,
                        targetRestSeconds = 75,
                        recommendedAlternative = "Bulgarian Split Squat / Hack Squat",
                        scienceNote = "Unilateral movements produce equal quad and glute hypertrophy while reducing axial spinal disc pressure by over 60%.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else if (hasOverheadPress) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        title = "Acromial Relief: Seated DB / Cable Shoulder Press",
                        explanation = "Replace Barbell Overhead Press with Seated Dumbbell Press or Cable Lateral Raises to preserve subacromial clearance.",
                        recommendedSets = 3,
                        recommendedReps = 12,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Seated Dumbbell Shoulder Press",
                        scienceNote = "Neutral or semi-pronated dumbbell grips prevent impingement of the supraspinatus tendon against the acromion.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else {
                // Proactive recommendation for 40+ athletes
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        title = "Longevity & Joint Biomechanics (Age ${profile.age}+)",
                        explanation = "Prioritize Dumbbell and Cable pathways over fixed straight barbells to respect your natural joint carrying angles and optimize tendon recovery.",
                        recommendedSets = profileGoalSets(profile.goal),
                        recommendedReps = 10,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Dumbbells & Cable Attachments",
                        scienceNote = "Synovial fluid replenishment and tendon collagen synthesis benefit significantly from multi-planar converging trajectories.",
                        tag = "JOINT_HEALTH"
                    )
                )
            }
        }

        return jointSuggestions
    }

    /**
     * Experience Level & Biometric Calibration.
     */
    private fun generateExperienceSuggestion(profile: UserAiProfile): AiExerciseSuggestion {
        val exp = profile.experienceLevel.lowercase()
        return when {
            exp.contains("beginner") || exp.contains("novice") -> {
                AiExerciseSuggestion(
                    title = "Neuromuscular Calibration (Beginner)",
                    explanation = "Focus on strict 3-second eccentric tempo and deliberate form. Rest an extra 15-30s per set to reset mental focus and motor patterns.",
                    recommendedSets = 3,
                    recommendedReps = 10,
                    targetRestSeconds = 75,
                    scienceNote = "Beginner strength gains are primarily neural (motor unit synchrony) rather than muscular, requiring controlled pacing.",
                    tag = "TECHNIQUE"
                )
            }
            exp.contains("advanced") || exp.contains("elite") -> {
                AiExerciseSuggestion(
                    title = "Intensity Density & Overload (Advanced)",
                    explanation = "Incorporate undulating load schemes and high density. Limit rest strictly to 60s on hypertrophy sets to induce peak cellular swelling.",
                    recommendedSets = 4,
                    recommendedReps = 8,
                    targetRestSeconds = 60,
                    scienceNote = "Advanced lifters require greater intra-workout training density and varied stimulus to overcome systemic adaptation plateaus.",
                    tag = "LOAD"
                )
            }
            else -> {
                AiExerciseSuggestion(
                    title = "Progressive Overload Cadence (Intermediate)",
                    explanation = "Aim for a double-progression model: when you reach the top of the 8-12 rep range on all sets, increase load by 2.5-5kg.",
                    recommendedSets = 4,
                    recommendedReps = 10,
                    targetRestSeconds = 60,
                    scienceNote = "Double progression guarantees systematic mechanical overload while preventing premature technical failure.",
                    tag = "LOAD"
                )
            }
        }
    }

    /**
     * Micro-optimization for an individual workout exercise.
     */
    fun generateExerciseSpecificSuggestion(
        exercise: WorkoutExercise,
        profile: UserAiProfile
    ): AiExerciseSuggestion? {
        val name = exercise.exerciseName.lowercase()

        if (profile.age >= 40 && name.contains("bench") && name.contains("barbell")) {
            return AiExerciseSuggestion(
                title = "Substitute ${exercise.exerciseName}",
                explanation = "Switch to Incline Dumbbell Press (30° incline) for safer shoulder kinematics and peak upper-pectoral fiber recruitment.",
                recommendedSets = 4,
                recommendedReps = 10,
                targetRestSeconds = 60,
                recommendedAlternative = "Incline Dumbbell Press",
                scienceNote = "Dumbbells permit converging wrist angles, reducing subacromial friction.",
                tag = "JOINT_HEALTH"
            )
        }

        if (name.contains("deadlift") && (profile.age >= 40 || profile.bmi > 28f)) {
            return AiExerciseSuggestion(
                title = "Lower Back Optimization: Trap Bar Deadlift",
                explanation = "A hexagonal Trap Bar places the center of gravity inside your base of support, reducing spinal shear stress while lifting heavy.",
                recommendedSets = 4,
                recommendedReps = 6,
                targetRestSeconds = 90,
                recommendedAlternative = "Trap Bar (Hex Bar) Deadlift",
                scienceNote = "Peak lumbar moment arm is reduced by ~22% compared to conventional straight-bar deadlifting.",
                tag = "JOINT_HEALTH"
            )
        }

        return null
    }

    /**
     * Helper to retrieve recommended sets for a goal.
     */
    private fun profileGoalSets(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 5
            g.contains("fat") || g.contains("tone") -> 3
            else -> 4
        }
    }

    /**
     * Helper to retrieve recommended reps for a goal.
     */
    fun getRecommendedRepsForGoal(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 5
            g.contains("fat") || g.contains("tone") -> 14
            else -> 10
        }
    }

    /**
     * Helper to retrieve target rest seconds for a goal.
     */
    fun getRecommendedRestForGoal(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 120
            g.contains("fat") || g.contains("tone") -> 45
            else -> 60
        }
    }
}

