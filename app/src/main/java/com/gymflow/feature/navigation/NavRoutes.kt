package com.gymflow.feature.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val FORGOT_PASSWORD = "auth/forgot_password"

    const val MEMBER_HOME = "member/home"
    const val WORKOUT_SCHEDULE = "member/schedule"
    const val EXERCISE_DETAILS_BASE = "member/exercise"
    const val EXERCISE_DETAILS = "member/exercise/{exerciseId}"
    const val ACTIVE_WORKOUT_BASE = "member/active_workout"
    const val ACTIVE_WORKOUT = "member/active_workout/{exerciseId}"
    const val REST_TIMER_BASE = "member/rest_timer"
    const val REST_TIMER = "member/rest_timer/{nextSetText}/{durationSeconds}"

    const val MEMBER_PROGRESS = "member/progress"
    const val MEMBER_ACTIVITY = "member/activity"
    const val MEMBER_PROFILE = "member/profile"
    const val MEMBER_MEASUREMENTS = "member/measurements"
    const val MEMBER_SETTINGS = "member/settings"
    const val MEMBER_JOIN_TRAINER = "member/join_trainer"
    const val MEMBER_WEIGHT = "member/weight"
    const val MEMBER_HYDRATION = "member/hydration"
    const val MEMBER_ACHIEVEMENTS = "member/achievements"
    const val MEMBER_STOPWATCH = "member/stopwatch"

    fun exerciseDetails(exerciseId: String): String = "$EXERCISE_DETAILS_BASE/$exerciseId"
    fun activeWorkout(exerciseId: String): String = "$ACTIVE_WORKOUT_BASE/$exerciseId"
    fun restTimer(nextSetText: String, durationSeconds: Int = 45): String {
        val encodedText = java.net.URLEncoder.encode(nextSetText, "UTF-8")
        return "$REST_TIMER_BASE/$encodedText/$durationSeconds"
    }
}
