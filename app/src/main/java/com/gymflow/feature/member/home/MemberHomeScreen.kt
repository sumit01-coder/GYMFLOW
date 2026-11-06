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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.MetricCard
import com.gymflow.core.firebase.FirestoreConnectionStatus
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowRed
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.feature.member.exercises.ExerciseThumbnail
import com.gymflow.feature.member.exercises.ExerciseVisuals

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
            if (uiState.todayExercises.isNotEmpty() || uiState.hasActiveSession) {
                val firstUncompleted = uiState.todayExercises.firstOrNull { !uiState.completedExerciseIds.contains(it.exerciseId) }
                val targetExerciseId = firstUncompleted?.exerciseId ?: uiState.todayExercises.firstOrNull()?.exerciseId ?: "ex_chest_press"
                onStartWorkout(targetExerciseId)
            } else {
                onNavigateToSchedule()
            }
        },
        onDiscardActiveSession = {
            viewModel.discardActiveSession()
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
    onDiscardActiveSession: () -> Unit = {},
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
                    // Cloud Connection Status Pill
                    val (statusText, statusBg, statusColor) = when (uiState.connectionStatus) {
                        FirestoreConnectionStatus.CONNECTED -> Triple("Cloud Live", Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
                        FirestoreConnectionStatus.SYNCING -> Triple("Syncing...", Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
                        FirestoreConnectionStatus.OFFLINE -> Triple("Offline", Color.Gray.copy(alpha = 0.15f), Color.Gray)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = statusText,
                            style = GymFlowTheme.typography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

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

            // 7-Day Email Verification Grace Period Reminder Banner
            if (!uiState.isEmailVerified) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .clickable { onNavigateToProfile() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Verify Email (${uiState.graceDaysRemaining}d remaining in grace period)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Tap to confirm verification before access is restricted.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

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
            if (uiState.todayExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No workouts scheduled for today. Check your schedule or start an open training session.",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                uiState.todayExercises.forEach { exercise ->
                    val isCompleted = uiState.completedExerciseIds.contains(exercise.exerciseId)
                    TodayExerciseItem(
                        exercise = exercise,
                        isCompleted = isCompleted,
                        onClick = { onNavigateToExerciseDetails(exercise.exerciseId) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CTA: Start Workout, Resume Paused Workout, or Next Exercise
            val completedCount = uiState.todayExercises.count { uiState.completedExerciseIds.contains(it.exerciseId) }
            val nextUncompleted = uiState.todayExercises.firstOrNull { !uiState.completedExerciseIds.contains(it.exerciseId) }
            val isAllCompleted = completedCount >= uiState.todayExercises.size && uiState.todayExercises.isNotEmpty()

            var showDiscardDialog by remember { mutableStateOf(false) }

            if (uiState.hasActiveSession && !isAllCompleted) {
                val mins = uiState.activeSessionDurationSeconds / 60
                val secs = uiState.activeSessionDurationSeconds % 60
                val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onStartWorkout() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WORKOUT IN PROGRESS · $timeFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Resume",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onStartWorkout() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showDiscardDialog = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (showDiscardDialog) {
                AlertDialog(
                    onDismissRequest = { showDiscardDialog = false },
                    title = {
                        Text(
                            text = "Discard Active Workout?",
                            style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "This will clear your in-progress workout session for today. Uncompleted sets will be reset.",
                            style = GymFlowTheme.typography.BodyMedium,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDiscardDialog = false
                                onDiscardActiveSession()
                            }
                        ) {
                            Text(
                                text = "Discard",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDiscardDialog = false }) {
                            Text(text = "Cancel", color = GymFlowTheme.colors.textSecondary)
                        }
                    },
                    containerColor = GymFlowTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            val buttonText = when {
                isAllCompleted -> "Workout Completed ✓"
                uiState.hasActiveSession -> "Resume Workout: ${nextUncompleted?.exerciseName ?: "Continue"}"
                uiState.todayExercises.isEmpty() -> "View Workout Schedule"
                completedCount > 0 -> "Continue Workout: ${nextUncompleted?.exerciseName ?: "Next Exercise"}"
                else -> "Start Workout"
            }

            GymFlowButton(
                text = buttonText,
                onClick = onStartWorkout,
                trailingIcon = {
                    Icon(
                        imageVector = when {
                            isAllCompleted -> Icons.Default.Check
                            uiState.todayExercises.isEmpty() && !uiState.hasActiveSession -> Icons.Default.DateRange
                            else -> Icons.Default.PlayArrow
                        },
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
    isCompleted: Boolean = false,
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise Demonstration Thumbnail
                ExerciseThumbnail(
                    imageUrl = ExerciseVisuals.getImageUrl(exercise.exerciseId),
                    category = exercise.category,
                    equipment = "Machine",
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = exercise.exerciseName,
                        style = GymFlowTheme.typography.BodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = GymFlowTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (exercise.category.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GymFlowBlack)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = exercise.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Text(
                            text = "${exercise.targetSets} sets · ${exercise.targetReps} reps · ${exercise.targetWeightKg} kg" +
                                    if (exercise.notes.isNotBlank()) " · ${exercise.notes}" else "",
                            style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                            color = GymFlowTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isCompleted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GymFlowGreen.copy(alpha = 0.12f))
                        .border(1.dp, GymFlowGreen.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        modifier = Modifier.size(13.dp),
                        tint = GymFlowGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "COMPLETED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    modifier = Modifier.size(20.dp),
                    tint = GymFlowTheme.colors.textTertiary
                )
            }
        }
    }
}
