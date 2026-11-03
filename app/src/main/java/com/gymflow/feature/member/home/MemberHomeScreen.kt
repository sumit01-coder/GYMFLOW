package com.gymflow.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.MetricCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowRed
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.domain.model.WorkoutExercise

@Composable
fun MemberHomeScreen(
    viewModel: MemberHomeViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExerciseDetails: (String) -> Unit,
    onStartWorkout: (String) -> Unit,
    onNavigateToWeight: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    MemberHomeContent(
        uiState = uiState,
        onNavigateToSchedule = onNavigateToSchedule,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToActivity = onNavigateToActivity,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToExerciseDetails = onNavigateToExerciseDetails,
        onNavigateToWeight = onNavigateToWeight,
        onNavigateToAchievements = onNavigateToAchievements,
        onStartWorkout = {
            val firstExerciseId = uiState.todayExercises.firstOrNull()?.exerciseId ?: "ex_chest_press"
            onStartWorkout(firstExerciseId)
        },
        modifier = modifier
    )
}

@Composable
fun MemberHomeContent(
    uiState: MemberHomeUiState,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExerciseDetails: (String) -> Unit,
    onStartWorkout: () -> Unit,
    onNavigateToWeight: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            GymFlowBottomBar(
                currentRoute = "member/home",
                onNavigate = { route ->
                    when (route) {
                        "member/schedule" -> onNavigateToSchedule()
                        "member/progress" -> onNavigateToProgress()
                        "member/activity" -> onNavigateToActivity()
                        "member/profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GYMFLOW",
                        style = GymFlowTheme.typography.Heading2.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GymFlowBadge(
                        text = "MEMBER",
                        backgroundColor = GymFlowTheme.colors.surfaceVariant,
                        textColor = GymFlowTheme.colors.textSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Icon with Unread Dot
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GymFlowTheme.colors.surfaceVariant)
                            .clickable { /* Notifications */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(20.dp),
                            tint = GymFlowTheme.colors.textPrimary
                        )
                        if (uiState.hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .background(GymFlowRed, shape = CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // User Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GymFlowBlack)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.userInitials,
                            style = GymFlowTheme.typography.Caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Greeting Section
            Text(
                text = uiState.greeting,
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 26.sp),
                color = GymFlowTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.motivationalSubtext,
                style = GymFlowTheme.typography.Subtitle,
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Stat Cards in 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Current Weight",
                    value = uiState.currentWeightKg,
                    unit = "kg",
                    icon = Icons.Default.MonitorWeight,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToWeight() }
                )
                MetricCard(
                    label = "Calories Today",
                    value = uiState.caloriesToday,
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToProgress() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Exercises",
                    value = uiState.exercisesProgress,
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSchedule() }
                )
                MetricCard(
                    label = "Workout Duration",
                    value = uiState.workoutDurationMinutes,
                    unit = "min",
                    icon = Icons.Default.Timer,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToActivity() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Today's Workout Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Workout",
                        style = GymFlowTheme.typography.Heading2,
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = uiState.todayWorkoutTitle,
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                Text(
                    text = "View All",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GymFlowTheme.colors.textPrimary,
                    modifier = Modifier.clickable { onNavigateToSchedule() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exercise items
            uiState.todayExercises.forEach { exercise ->
                TodayExerciseItem(
                    exercise = exercise,
                    onClick = { onNavigateToExerciseDetails(exercise.exerciseId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CTA: Start Workout ▶
            GymFlowButton(
                text = "Start Workout",
                onClick = onStartWorkout,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GymFlowWhite
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TodayExerciseItem(
    exercise: WorkoutExercise,
    onClick: () -> Unit
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise Thumbnail Placeholder / Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(GymFlowShapesTokens.CardSmall)
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .border(1.dp, GymFlowBorder, GymFlowShapesTokens.CardSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = GymFlowBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = exercise.exerciseName,
                        style = GymFlowTheme.typography.BodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${exercise.targetSets} sets · ${exercise.targetReps} reps · ${exercise.targetWeightKg.toInt()} kg",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                modifier = Modifier.size(20.dp),
                tint = GymFlowTheme.colors.textTertiary
            )
        }
    }
}
