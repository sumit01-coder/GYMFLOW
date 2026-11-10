package com.gymflow.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymflow.domain.model.Role
import com.gymflow.feature.auth.AuthViewModel
import com.gymflow.feature.auth.EmailVerificationScreen
import com.gymflow.feature.auth.ForgotPasswordScreen
import com.gymflow.feature.auth.LoginScreen
import com.gymflow.feature.auth.RegisterScreen
import com.gymflow.feature.auth.SplashScreen
import com.gymflow.feature.trainer.navigation.TrainerRoute
import com.gymflow.feature.trainer.navigation.trainerNavGraph

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.SPLASH
) {
    LaunchedEffect(Unit) {
        AppNavigator.navEvents.collect { route ->
            try {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                // Route navigation will take place once session/graph is ready
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Screen 01: Splash Screen
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.MEMBER_HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToTrainerDashboard = {
                    navController.navigate(TrainerRoute.Dashboard) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Screen 02: Login Screen
        composable(NavRoutes.LOGIN) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateBack = null,
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
                },
                onNavigateToEmailVerification = {
                    navController.navigate(NavRoutes.EMAIL_VERIFICATION)
                },
                onLoginSuccess = { role ->
                    if (role == Role.TRAINER) {
                        navController.navigate(TrainerRoute.Dashboard) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.MEMBER_HOME) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Auth: Register Screen
        composable(NavRoutes.REGISTER) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { _ ->
                    navController.navigate(NavRoutes.EMAIL_VERIFICATION) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // Auth: Email Verification Screen
        composable(NavRoutes.EMAIL_VERIFICATION) {
            val authViewModel: AuthViewModel = hiltViewModel()
            EmailVerificationScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.EMAIL_VERIFICATION) { inclusive = true }
                    }
                },
                onVerificationSuccess = { role ->
                    if (role == Role.TRAINER) {
                        navController.navigate(TrainerRoute.Dashboard) {
                            popUpTo(NavRoutes.EMAIL_VERIFICATION) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.MEMBER_HOME) {
                            popUpTo(NavRoutes.EMAIL_VERIFICATION) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Auth: Forgot Password Screen
        composable(NavRoutes.FORGOT_PASSWORD) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Member Subgraph (Screens 03 to 14)
        memberNavGraph(navController = navController)

        // Trainer Studio Subgraph (Screens 15 to 25)
        trainerNavGraph(
            navController = navController,
            onLogout = {
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
