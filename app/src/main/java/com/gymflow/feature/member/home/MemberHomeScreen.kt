package com.gymflow.feature.member.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
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
    onNavigateToExplore: (String) -> Unit = {},
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
        onNavigateToExplore = onNavigateToExplore,
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
        onStartOutdoor = { type -> viewModel.startOutdoorTracking(type) },
        onPauseOutdoor = { viewModel.pauseOutdoorTracking() },
        onResumeOutdoor = { viewModel.resumeOutdoorTracking() },
        onFinishOutdoor = { viewModel.finishOutdoorTracking() },
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
    onNavigateToExplore: (String) -> Unit = {},
    onDiscardActiveSession: () -> Unit = {},
    onStartOutdoor: (String) -> Unit = {},
    onPauseOutdoor: () -> Unit = {},
    onResumeOutdoor: () -> Unit = {},
    onFinishOutdoor: () -> Unit = {},
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

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Steps & Outdoor Cardio Card
            DailyStepsAndOutdoorCard(
                steps = uiState.todaySteps,
                stepGoal = uiState.stepGoal,
                progressFraction = uiState.stepProgressFraction,
                distanceKm = uiState.stepDistanceKm,
                calories = uiState.stepCalories,
                isOutdoorTracking = uiState.isOutdoorTracking,
                isOutdoorPaused = uiState.isOutdoorPaused,
                outdoorDurationSeconds = uiState.outdoorDurationSeconds,
                outdoorDistanceMeters = uiState.outdoorDistanceMeters,
                outdoorPace = uiState.outdoorPace,
                outdoorWorkoutType = uiState.outdoorWorkoutType,
                onStartOutdoor = onStartOutdoor,
                onPauseOutdoor = onPauseOutdoor,
                onResumeOutdoor = onResumeOutdoor,
                onFinishOutdoor = onFinishOutdoor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Explore Exercises Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Explore Exercises",
                        style = GymFlowTheme.typography.Heading2,
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = "Browse form cues, muscle targets & stretches",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                Text(
                    text = "Explore All →",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GymFlowTheme.colors.textPrimary,
                    modifier = Modifier.clickable { onNavigateToExplore("ALL") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Muscle Category Cards with animated anatomical form visuals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseVisuals.CATEGORY_FORM_GUIDES.forEach { category ->
                    Box(
                        modifier = Modifier
                            .width(165.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GymFlowTheme.colors.surface)
                            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToExplore(category.categoryKey) }
                            .padding(12.dp)
                    ) {
                        Column {
                            // Top header row: Muscle badge + Chevron
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (category.categoryKey == "STRETCHING") Color(0xFFEDE9FE) else GymFlowTheme.colors.surfaceVariant)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = category.muscleBadge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (category.categoryKey == "STRETCHING") Color(0xFF7C3AED) else GymFlowTheme.colors.textPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = GymFlowTheme.colors.textTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Form Guide Visual Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GymFlowTheme.colors.surfaceVariant.copy(alpha = 0.55f)),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(category.formGifUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${category.title} Form Guide",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = GymFlowBlack
                                            )
                                        }
                                    },
                                    error = {
                                        Icon(
                                            imageVector = if (category.categoryKey == "STRETCHING") Icons.Default.SelfImprovement else Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = GymFlowTheme.colors.textSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                )

                                // Form Guide Pill tag overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(5.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(GymFlowGreen)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "FORM",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GymFlowWhite,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = category.title,
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = GymFlowTheme.colors.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = category.subtitle,
                                style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                                color = GymFlowTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD97706))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ACTIVE WORKOUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = timeFormatted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Discard Button Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEE2E2))
                                .clickable { showDiscardDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Discard",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Discard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Resume Button Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymFlowBlack)
                                .clickable { onStartWorkout() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Resume",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                uiState.hasActiveSession && uiState.todayExercises.isNotEmpty() -> "Resume Workout: ${nextUncompleted?.exerciseName ?: "Continue"}"
                uiState.hasActiveSession && uiState.todayExercises.isEmpty() -> "Resume Open Session"
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

@Composable
fun DailyStepsAndOutdoorCard(
    steps: Int,
    stepGoal: Int,
    progressFraction: Float,
    distanceKm: Float,
    calories: Float,
    isOutdoorTracking: Boolean,
    isOutdoorPaused: Boolean,
    outdoorDurationSeconds: Long,
    outdoorDistanceMeters: Float,
    outdoorPace: String,
    outdoorWorkoutType: String,
    onStartOutdoor: (String) -> Unit,
    onPauseOutdoor: () -> Unit,
    onResumeOutdoor: () -> Unit,
    onFinishOutdoor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var pendingWorkoutType by remember { mutableStateOf("RUN") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasLocationPermission = fineGranted
        if (fineGranted) {
            onStartOutdoor(pendingWorkoutType)
        }
    }

    fun launchOutdoorSession(type: String) {
        pendingWorkoutType = type
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            onStartOutdoor(type)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val cardShape = RoundedCornerShape(18.dp)

    if (isOutdoorTracking) {
        // ACTIVE OUTDOOR GPS HUD CARD
        val mins = outdoorDurationSeconds / 60
        val secs = outdoorDurationSeconds % 60
        val formattedTime = "%02d:%02d".format(mins, secs)
        val formattedDist = "%.2f".format(outdoorDistanceMeters / 1000f)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(GymFlowBlack)
                .border(1.5.dp, GymFlowGreen, cardShape)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GymFlowGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OUTDOOR GPS CARDIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GymFlowGreen,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isOutdoorPaused) "Paused" else "Tracking Route & Cadence",
                                fontSize = 12.sp,
                                color = GymFlowWhite.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isOutdoorPaused) Color(0xFFD97706) else GymFlowGreen)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isOutdoorPaused) "PAUSED" else "LIVE GPS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large Duration Display
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = GymFlowWhite,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // HUD Metrics Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2024))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DISTANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$formattedDist km",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite
                        )
                    }

                    Column {
                        Text(
                            text = "PACE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$outdoorPace /km",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite
                        )
                    }

                    Column {
                        Text(
                            text = "ACTIVITY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowWhite.copy(alpha = 0.6f)
                        )
                        Text(
                            text = outdoorWorkoutType,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Controls: Pause/Resume + Finish
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2D32))
                            .clickable {
                                if (isOutdoorPaused) onResumeOutdoor() else onPauseOutdoor()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOutdoorPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOutdoorPaused) "Resume" else "Pause",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDC2626))
                            .clickable { onFinishOutdoor() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Finish Session",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite
                            )
                        }
                    }
                }
            }
        }
    } else {
        // AMBIENT DAILY STEPS CARD
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(GymFlowTheme.colors.surface)
                .border(1.dp, GymFlowBorder, cardShape)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GymFlowTheme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = GymFlowTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daily Steps & Movement",
                                style = GymFlowTheme.typography.BodyMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GymFlowTheme.colors.textPrimary
                            )
                            Text(
                                text = "Hardware step sensor · Auto synced",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                    }

                    // Progress % badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GymFlowGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, GymFlowGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steps Count Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "%,d".format(steps),
                        style = GymFlowTheme.typography.Heading1.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ %,d steps".format(stepGoal),
                        style = GymFlowTheme.typography.BodyMedium,
                        color = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GymFlowGreen,
                    trackColor = GymFlowTheme.colors.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats: Distance & Calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.2f km".format(distanceKm),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.0f kcal".format(calories),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action: Start Outdoor Cardio (Run / Walk)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GymFlowBlack)
                            .clickable { launchOutdoorSession("RUN") }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Outdoor Run",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GymFlowTheme.colors.surfaceVariant)
                            .border(1.dp, GymFlowBorder, RoundedCornerShape(12.dp))
                            .clickable { launchOutdoorSession("WALK") }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = GymFlowTheme.colors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Outdoor Walk",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
