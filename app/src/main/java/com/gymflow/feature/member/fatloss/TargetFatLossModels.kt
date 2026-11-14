package com.gymflow.feature.member.fatloss

data class TargetZoneExercise(
    val name: String,
    val targetMuscle: String,
    val repsOrDuration: String,
    val sets: Int = 3,
    val instruction: String,
    val gifUrl: String? = null
)

data class TargetFatLossZone(
    val id: String,
    val name: String,
    val subtitle: String,
    val bodyView: BodyView, // FRONT or BACK
    val anatomyHighlight: String,
    val scienceOverview: String,
    val realisticTimelineWeeks: Int = 8,
    val recommendedCaloricDeficitKcal: Int = 450,
    val heroGifUrl: String? = null,
    val nutritionKeyAdvice: List<String>,
    val hiitBurnerTitle: String,
    val hiitBurnerDurationMin: Int = 15,
    val hiitCircuitExercises: List<TargetZoneExercise>,
    val firmingRoutineExercises: List<TargetZoneExercise>
)

enum class BodyView {
    FRONT,
    BACK
}
