package com.gymflow.feature.trainer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gymflow.feature.trainer.analytics.TrainerAnalyticsScreen
import com.gymflow.feature.trainer.auth.TrainerLoginScreen
import com.gymflow.feature.trainer.auth.TrainerSplashScreen
import com.gymflow.feature.trainer.dashboard.TrainerDashboardScreen
import com.gymflow.feature.trainer.exercise.TrainerExerciseLibraryScreen
import com.gymflow.feature.trainer.joincode.TrainerJoinCodeScreen
import com.gymflow.feature.trainer.members.MemberManagementScreen
import com.gymflow.feature.trainer.plans.ExerciseAssignmentScreen
import com.gymflow.feature.trainer.plans.WorkoutPlanBuilderScreen
import com.gymflow.feature.trainer.profile.TrainerMemberProfileScreen
import com.gymflow.feature.trainer.progress.TrainerMemberProgressScreen
import com.gymflow.feature.trainer.settings.TrainerSettingsScreen
import com.gymflow.feature.trainer.theme.TrainerTheme
import kotlinx.serialization.Serializable

sealed interface TrainerRoute {
    @Serializable
    data object Splash : TrainerRoute

    @Serializable
    data object Login : TrainerRoute

    @Serializable
    data object Dashboard : TrainerRoute

    @Serializable
    data object MemberManagement : TrainerRoute

    @Serializable
    data class MemberProfile(val memberId: String = "sarah-miller-101") : TrainerRoute

    @Serializable
    data class WorkoutPlanBuilder(val memberId: String = "sarah-miller-101") : TrainerRoute

    @Serializable
    data class ExerciseAssignment(
        val dayOfWeek: String = "Monday",
        val planId: String = "plan-hypertrophy-1"
    ) : TrainerRoute

    @Serializable
    data class ExerciseLibrary(val isSelectionMode: Boolean = false) : TrainerRoute

    @Serializable
    data class MemberProgress(val memberId: String = "sarah-miller-101") : TrainerRoute

    @Serializable
    data object Analytics : TrainerRoute

    @Serializable
    data object JoinCode : TrainerRoute

    @Serializable
    data object Settings : TrainerRoute
}

fun NavGraphBuilder.trainerNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit = {
        navController.navigate(TrainerRoute.Login) {
            popUpTo(TrainerRoute.Dashboard) { inclusive = true }
        }
    }
) {
    // Screen 15: Trainer Splash
    composable<TrainerRoute.Splash> {
        TrainerSplashScreen(
            onNavigateToLogin = {
                navController.navigate(TrainerRoute.Login)
            },
            onNavigateToRegister = {
                navController.navigate(TrainerRoute.Login)
            }
        )
    }

    // Screen 16: Trainer Login
    composable<TrainerRoute.Login> {
        TrainerLoginScreen(
            onLoginSuccess = {
                navController.navigate(TrainerRoute.Dashboard) {
                    popUpTo(TrainerRoute.Login) { inclusive = true }
                }
            },
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

    // Screen 17: Trainer Dashboard
    composable<TrainerRoute.Dashboard> {
        TrainerDashboardScreen(
            onCreatePlanClick = {
                navController.navigate(TrainerRoute.WorkoutPlanBuilder("sarah-miller-101"))
            },
            onNavigateToMembers = {
                navController.navigate(TrainerRoute.MemberManagement)
            },
            onNavigateToMemberProfile = { memberId ->
                navController.navigate(TrainerRoute.MemberProfile(memberId))
            },
            onNavigateToPlanBuilder = {
                navController.navigate(TrainerRoute.WorkoutPlanBuilder("sarah-miller-101"))
            },
            onNavigateToExerciseLibrary = {
                navController.navigate(TrainerRoute.ExerciseLibrary())
            },
            onNavigateToAnalytics = {
                navController.navigate(TrainerRoute.Analytics)
            },
            onNavigateToJoinCode = {
                navController.navigate(TrainerRoute.JoinCode)
            },
            onNavigateToSettings = {
                navController.navigate(TrainerRoute.Settings)
            }
        )
    }

    // Screen 18: Member Management
    composable<TrainerRoute.MemberManagement> {
        MemberManagementScreen(
            onBackClick = { navController.popBackStack() },
            onMemberClick = { memberId ->
                navController.navigate(TrainerRoute.MemberProfile(memberId))
            },
            onViewProgressClick = { memberId ->
                navController.navigate(TrainerRoute.MemberProgress(memberId))
            },
            onCreatePlanForMember = { memberId ->
                navController.navigate(TrainerRoute.WorkoutPlanBuilder(memberId))
            },
            onNavigateToDashboard = { navController.navigate(TrainerRoute.Dashboard) },
            onNavigateToPlans = { navController.navigate(TrainerRoute.WorkoutPlanBuilder()) },
            onNavigateToAnalytics = { navController.navigate(TrainerRoute.Analytics) },
            onNavigateToSettings = { navController.navigate(TrainerRoute.Settings) }
        )
    }

    // Screen 19: Member Profile (Sarah Miller)
    composable<TrainerRoute.MemberProfile> { backStackEntry ->
        val route = backStackEntry.toRoute<TrainerRoute.MemberProfile>()
        TrainerMemberProfileScreen(
            memberId = route.memberId,
            onBackClick = { navController.popBackStack() },
            onViewFullProgress = {
                navController.navigate(TrainerRoute.MemberProgress(route.memberId))
            },
            onEditWorkoutPlan = {
                navController.navigate(TrainerRoute.WorkoutPlanBuilder(route.memberId))
            }
        )
    }

    // Screen 20: Workout Plan Builder
    composable<TrainerRoute.WorkoutPlanBuilder> { backStackEntry ->
        val route = backStackEntry.toRoute<TrainerRoute.WorkoutPlanBuilder>()
        WorkoutPlanBuilderScreen(
            initialMemberId = route.memberId,
            onBackClick = { navController.popBackStack() },
            onConfigureDayExercises = { dayOfWeek, planId ->
                navController.navigate(TrainerRoute.ExerciseAssignment(dayOfWeek, planId))
            },
            onOpenExerciseLibrary = {
                navController.navigate(TrainerRoute.ExerciseLibrary(isSelectionMode = true))
            },
            onPlanSaved = {
                navController.navigate(TrainerRoute.Dashboard) {
                    popUpTo(TrainerRoute.Dashboard) { inclusive = false }
                }
            },
            onNavigateToDashboard = { navController.navigate(TrainerRoute.Dashboard) },
            onNavigateToMembers = { navController.navigate(TrainerRoute.MemberManagement) },
            onNavigateToAnalytics = { navController.navigate(TrainerRoute.Analytics) },
            onNavigateToSettings = { navController.navigate(TrainerRoute.Settings) }
        )
    }

    // Screen 20 Sub-screen: Exercise Assignment Editor
    composable<TrainerRoute.ExerciseAssignment> { backStackEntry ->
        val route = backStackEntry.toRoute<TrainerRoute.ExerciseAssignment>()
        ExerciseAssignmentScreen(
            dayOfWeek = route.dayOfWeek,
            planId = route.planId,
            onBackClick = { navController.popBackStack() },
            onAddExerciseFromLibrary = {
                navController.navigate(TrainerRoute.ExerciseLibrary(isSelectionMode = true))
            },
            onSaved = { navController.popBackStack() }
        )
    }

    // Screen 21: Exercise Library
    composable<TrainerRoute.ExerciseLibrary> { backStackEntry ->
        val route = backStackEntry.toRoute<TrainerRoute.ExerciseLibrary>()
        TrainerExerciseLibraryScreen(
            isSelectionMode = route.isSelectionMode,
            onBackClick = { navController.popBackStack() },
            onExerciseSelected = { _ ->
                if (route.isSelectionMode) {
                    navController.popBackStack()
                }
            }
        )
    }

    // Screen 22: Member Progress View
    composable<TrainerRoute.MemberProgress> { backStackEntry ->
        val route = backStackEntry.toRoute<TrainerRoute.MemberProgress>()
        TrainerMemberProgressScreen(
            memberId = route.memberId,
            onBackClick = { navController.popBackStack() },
            onEditPlanClick = {
                navController.navigate(TrainerRoute.WorkoutPlanBuilder(route.memberId))
            }
        )
    }

    // Screen 23: Studio Analytics
    composable<TrainerRoute.Analytics> {
        TrainerAnalyticsScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToDashboard = { navController.navigate(TrainerRoute.Dashboard) },
            onNavigateToMembers = { navController.navigate(TrainerRoute.MemberManagement) },
            onNavigateToPlans = { navController.navigate(TrainerRoute.WorkoutPlanBuilder()) },
            onNavigateToSettings = { navController.navigate(TrainerRoute.Settings) }
        )
    }

    // Screen 24: Trainer Join Code
    composable<TrainerRoute.JoinCode> {
        TrainerJoinCodeScreen(
            onBackClick = { navController.popBackStack() },
            onMemberApproved = {
                // Handled in ViewModel / Navigates to member management
            }
        )
    }

    // Screen 25: Trainer Settings
    composable<TrainerRoute.Settings> {
        TrainerSettingsScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToMembers = { navController.navigate(TrainerRoute.MemberManagement) },
            onNavigateToJoinCode = { navController.navigate(TrainerRoute.JoinCode) },
            onNavigateToAnalytics = { navController.navigate(TrainerRoute.Analytics) },
            onLogout = onLogout,
            onNavigateToDashboard = { navController.navigate(TrainerRoute.Dashboard) },
            onNavigateToPlans = { navController.navigate(TrainerRoute.WorkoutPlanBuilder()) }
        )
    }
}

@Composable
fun TrainerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = TrainerRoute.Splash,
    onLogoutToMain: () -> Unit = {
        navController.navigate(TrainerRoute.Login) {
            popUpTo(TrainerRoute.Dashboard) { inclusive = true }
        }
    }
) {
    TrainerTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            trainerNavGraph(
                navController = navController,
                onLogout = onLogoutToMain
            )
        }
    }
}
