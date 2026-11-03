package com.gymflow.feature.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gymflow.feature.member.activity.MemberActivityScreen
import com.gymflow.feature.member.exercises.ExerciseDetailsScreen
import com.gymflow.feature.member.exercises.ExerciseDetailsViewModel
import com.gymflow.feature.member.home.MemberHomeScreen
import com.gymflow.feature.member.home.MemberHomeViewModel
import com.gymflow.feature.member.measurements.BodyMeasurementsScreen
import com.gymflow.feature.member.profile.MemberProfileScreen
import com.gymflow.feature.member.profile.MemberSettingsScreen
import com.gymflow.feature.member.progress.MemberProgressScreen
import com.gymflow.feature.member.schedule.WorkoutScheduleScreen
import com.gymflow.feature.member.schedule.WorkoutScheduleViewModel
import com.gymflow.feature.member.trainer.JoinTrainerScreen
import com.gymflow.feature.member.weight.WeightTrackingScreen
import com.gymflow.feature.member.workout.ActiveWorkoutScreen
import com.gymflow.feature.member.workout.ActiveWorkoutViewModel
import com.gymflow.feature.member.workout.RestTimerScreen
import com.gymflow.feature.member.workout.RestTimerViewModel

fun NavGraphBuilder.memberNavGraph(navController: NavController) {
    // Screen 03: Member Home
    composable(NavRoutes.MEMBER_HOME) {
        val viewModel: MemberHomeViewModel = hiltViewModel()
        MemberHomeScreen(
            viewModel = viewModel,
            onNavigateToSchedule = { navController.navigate(NavRoutes.WORKOUT_SCHEDULE) },
            onNavigateToProgress = { navController.navigate(NavRoutes.MEMBER_PROGRESS) },
            onNavigateToActivity = { navController.navigate(NavRoutes.MEMBER_ACTIVITY) },
            onNavigateToProfile = { navController.navigate(NavRoutes.MEMBER_PROFILE) },
            onNavigateToExerciseDetails = { exerciseId ->
                navController.navigate(NavRoutes.exerciseDetails(exerciseId))
            },
            onStartWorkout = { exerciseId ->
                navController.navigate(NavRoutes.activeWorkout(exerciseId))
            }
        )
    }

    // Screen 04: Workout Schedule
    composable(NavRoutes.WORKOUT_SCHEDULE) {
        val viewModel: WorkoutScheduleViewModel = hiltViewModel()
        WorkoutScheduleScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHome = { navController.navigate(NavRoutes.MEMBER_HOME) },
            onNavigateToProgress = { navController.navigate(NavRoutes.MEMBER_PROGRESS) },
            onNavigateToActivity = { navController.navigate(NavRoutes.MEMBER_ACTIVITY) },
            onNavigateToProfile = { navController.navigate(NavRoutes.MEMBER_PROFILE) },
            onSelectWorkout = { workoutTitle ->
                navController.navigate(NavRoutes.exerciseDetails("ex_chest_press"))
            }
        )
    }

    // Screen 05: Exercise Details
    composable(
        route = NavRoutes.EXERCISE_DETAILS,
        arguments = listOf(
            navArgument("exerciseId") {
                type = NavType.StringType
                defaultValue = "ex_chest_press"
            }
        )
    ) {
        val viewModel: ExerciseDetailsViewModel = hiltViewModel()
        ExerciseDetailsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onStartSet = { exerciseId ->
                navController.navigate(NavRoutes.activeWorkout(exerciseId))
            }
        )
    }

    // Screen 06: Active Workout
    composable(
        route = NavRoutes.ACTIVE_WORKOUT,
        arguments = listOf(
            navArgument("exerciseId") {
                type = NavType.StringType
                defaultValue = "ex_chest_press"
            }
        )
    ) {
        val viewModel: ActiveWorkoutViewModel = hiltViewModel()
        ActiveWorkoutScreen(
            viewModel = viewModel,
            onCloseWorkout = { navController.popBackStack(NavRoutes.MEMBER_HOME, inclusive = false) },
            onNavigateToRest = { nextSetText, durationSeconds ->
                navController.navigate(NavRoutes.restTimer(nextSetText, durationSeconds))
            }
        )
    }

    // Screen 07: Rest Timer
    composable(
        route = NavRoutes.REST_TIMER,
        arguments = listOf(
            navArgument("nextSetText") {
                type = NavType.StringType
                defaultValue = "Set 3 of 3"
            },
            navArgument("durationSeconds") {
                type = NavType.StringType
                defaultValue = "45"
            }
        )
    ) {
        val viewModel: RestTimerViewModel = hiltViewModel()
        RestTimerScreen(
            viewModel = viewModel,
            onSkipRest = { navController.popBackStack() }
        )
    }

    // Member Progress
    composable(NavRoutes.MEMBER_PROGRESS) {
        MemberProgressScreen(
            onNavigateToHome = { navController.navigate(NavRoutes.MEMBER_HOME) },
            onNavigateToSchedule = { navController.navigate(NavRoutes.WORKOUT_SCHEDULE) },
            onNavigateToActivity = { navController.navigate(NavRoutes.MEMBER_ACTIVITY) },
            onNavigateToProfile = { navController.navigate(NavRoutes.MEMBER_PROFILE) }
        )
    }

    // Member Activity
    composable(NavRoutes.MEMBER_ACTIVITY) {
        MemberActivityScreen(
            onNavigateToHome = { navController.navigate(NavRoutes.MEMBER_HOME) },
            onNavigateToSchedule = { navController.navigate(NavRoutes.WORKOUT_SCHEDULE) },
            onNavigateToProgress = { navController.navigate(NavRoutes.MEMBER_PROGRESS) },
            onNavigateToProfile = { navController.navigate(NavRoutes.MEMBER_PROFILE) }
        )
    }

    // Member Profile
    composable(NavRoutes.MEMBER_PROFILE) {
        MemberProfileScreen(
            onNavigateToHome = { navController.navigate(NavRoutes.MEMBER_HOME) },
            onNavigateToSchedule = { navController.navigate(NavRoutes.WORKOUT_SCHEDULE) },
            onNavigateToProgress = { navController.navigate(NavRoutes.MEMBER_PROGRESS) },
            onNavigateToActivity = { navController.navigate(NavRoutes.MEMBER_ACTIVITY) },
            onNavigateToMeasurements = { navController.navigate(NavRoutes.MEMBER_MEASUREMENTS) },
            onNavigateToSettings = { navController.navigate(NavRoutes.MEMBER_SETTINGS) },
            onNavigateToJoinTrainer = { navController.navigate(NavRoutes.MEMBER_JOIN_TRAINER) },
            onLogout = {
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Body Measurements
    composable(NavRoutes.MEMBER_MEASUREMENTS) {
        BodyMeasurementsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Settings & Privacy
    composable(NavRoutes.MEMBER_SETTINGS) {
        MemberSettingsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateTrainerSync = { navController.navigate(NavRoutes.MEMBER_JOIN_TRAINER) },
            onLoggedOut = {
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Join / Connected Trainer
    composable(NavRoutes.MEMBER_JOIN_TRAINER) {
        JoinTrainerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Weight Tracking
    composable(NavRoutes.MEMBER_WEIGHT) {
        WeightTrackingScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
