package com.gymflow.feature.member.workout

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.components.ActiveWorkoutBadge
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.feature.member.exercises.ExerciseVisuals

@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onCloseWorkout: () -> Unit,
    onNavigateToRest: (nextSetText: String, durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ActiveWorkoutContent(
        uiState = uiState,
        onTogglePause = viewModel::togglePause,
        onIncrementReps = viewModel::incrementReps,
        onDecrementReps = viewModel::decrementReps,
        onIncrementWeight = viewModel::incrementWeight,
        onDecrementWeight = viewModel::decrementWeight,
        onCompleteSet = {
            viewModel.completeSet(onNavigateToRest)
        },
        onStartNextSet = viewModel::startNextSet,
        onOpenNextExercise = viewModel::openNextExercise,
        onFinishWorkout = {
            viewModel.finishWorkout(onCloseWorkout)
        },
        onCloseWorkout = onCloseWorkout,
        modifier = modifier
    )
}

@Composable
fun ActiveWorkoutContent(
    uiState: ActiveWorkoutUiState,
    onTogglePause: () -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onIncrementWeight: () -> Unit,
    onDecrementWeight: () -> Unit,
    onCompleteSet: () -> Unit,
    onStartNextSet: () -> Unit,
    onOpenNextExercise: () -> Unit,
    onFinishWorkout: () -> Unit,
    onCloseWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.activity.compose.BackHandler(onBack = onCloseWorkout)

    val progressAnim by animateFloatAsState(
        targetValue = uiState.progressPercentage,
        label = "workoutProgress"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymFlowTheme.colors.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                when {
                    uiState.isWorkoutComplete -> {
                        GymFlowButton(
                            text = "Finish Workout",
                            onClick = onFinishWorkout,
                            style = GymFlowButtonStyle.PRIMARY,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GymFlowWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All ${uiState.totalExercisesCount} exercises completed! Tap to finish and save.",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    uiState.isCurrentExerciseCompleted && uiState.hasNextExercise -> {
                        // Exercise completed -> Advance to Next Exercise
                        GymFlowButton(
                            text = "Next Exercise: ${uiState.nextExerciseTitle ?: "Continue"} ▶",
                            onClick = onOpenNextExercise,
                            style = GymFlowButtonStyle.PRIMARY,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GymFlowGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exercise ${uiState.currentExerciseIndex + 1} of ${uiState.totalExercisesCount} completed (✓) · Tap to start ${uiState.nextExerciseTitle}",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    uiState.isWaitingForNextSet -> {
                        // Controlled Advancement: User is in complete control before next set begins
                        GymFlowButton(
                            text = "Start Next Set (Set ${uiState.currentSet + 1})",
                            onClick = onStartNextSet,
                            style = GymFlowButtonStyle.PRIMARY,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = GymFlowWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rest period completed · Tap when physically ready to begin set ${uiState.currentSet + 1}",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        // Active Set Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GymFlowButton(
                                text = if (uiState.isPaused) "Resume" else "Pause",
                                onClick = onTogglePause,
                                style = GymFlowButtonStyle.OUTLINED,
                                modifier = Modifier.weight(0.40f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            GymFlowButton(
                                text = "Complete Set",
                                onClick = onCompleteSet,
                                style = GymFlowButtonStyle.PRIMARY,
                                modifier = Modifier.weight(0.60f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GymFlowWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Rest timer starts automatically after completing a set",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseWorkout) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Workout",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                ActiveWorkoutBadge()

                Text(
                    text = uiState.workoutTag,
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GymFlowTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator / Exercise Stepper
            if (uiState.totalExercisesCount > 1) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .border(1.dp, GymFlowBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCISE ${uiState.currentExerciseIndex + 1} OF ${uiState.totalExercisesCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Exercise Title
            Text(
                text = uiState.exerciseName,
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 28.sp),
                color = GymFlowTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )

            // Exercise Completed Checkmark Notice
            if (uiState.isCurrentExerciseCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCFCE7))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GymFlowGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Exercise Completed! ✓",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowGreen
                            )
                            Text(
                                text = if (uiState.hasNextExercise) "All ${uiState.totalSets} sets recorded. Tap below to start ${uiState.nextExerciseTitle}." else "All routine exercises completed! Tap Finish below to save.",
                                fontSize = 11.sp,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exercise Dynamic Movement Illustration Box (Full Height & Scaled without Clipping)
            val mediaUrl = ExerciseVisuals.getGifUrl(uiState.exerciseId) ?: ExerciseVisuals.getImageUrl(uiState.exerciseId)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, GymFlowBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!mediaUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = uiState.exerciseName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = GymFlowWhite,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            val fallbackStaticUrl = ExerciseVisuals.getImageUrl(uiState.exerciseId)
                            if (!fallbackStaticUrl.isNullOrBlank() && fallbackStaticUrl != mediaUrl) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(fallbackStaticUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = uiState.exerciseName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = GymFlowBlack,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.exerciseName,
                                        style = GymFlowTheme.typography.Caption,
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    )

                    // Form Guide Badge in top right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "FORM GUIDE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GymFlowBlack,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.exerciseName,
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exercise Duration Timer Display
            Text(
                text = "EXERCISE DURATION",
                style = GymFlowTheme.typography.Caption.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.formattedDuration,
                style = GymFlowTheme.typography.Timer.copy(fontSize = 44.sp),
                color = if (uiState.isPaused) GymFlowTheme.colors.accentPending else GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ready for Next Set recovery notice
            if (uiState.isWaitingForNextSet) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCFCE7).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Set ${uiState.currentSet} Complete & Logged",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rest period finished. Tap 'Start Next Set' below whenever you are ready.",
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Target Metrics (3 Columns: Sets, Reps, Target Weight)
            GymFlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SETS",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.totalSets}",
                            style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(GymFlowBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "REPS",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.targetReps}",
                            style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(GymFlowBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TARGET",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.targetWeightKg.toInt()} kg",
                            style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusText = when {
                        uiState.isWorkoutComplete -> "All ${uiState.totalSets} Sets Completed!"
                        uiState.isWaitingForNextSet -> "Waiting: Set ${uiState.currentSet + 1} of ${uiState.totalSets}"
                        else -> "Active Set: Set ${uiState.currentSet} of ${uiState.totalSets}"
                    }
                    Text(
                        text = statusText,
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = "${(progressAnim * 100).toInt()}%",
                        style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = GymFlowBlack,
                    trackColor = GymFlowTheme.colors.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actual Input Controls for Reps and Weight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Actual Reps Card
                GymFlowCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 12.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ACTUAL REPS",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowTheme.colors.surfaceVariant)
                                    .clickable { onDecrementReps() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Reps",
                                    tint = GymFlowTheme.colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "${uiState.actualReps}",
                                style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                                color = GymFlowTheme.colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowTheme.colors.surfaceVariant)
                                    .clickable { onIncrementReps() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Reps",
                                    tint = GymFlowTheme.colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Actual Weight Card
                GymFlowCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 12.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ACTUAL WEIGHT",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowTheme.colors.surfaceVariant)
                                    .clickable { onDecrementWeight() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Weight",
                                    tint = GymFlowTheme.colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            val weightLabel = if (uiState.actualWeightKg % 1f == 0f) {
                                "${uiState.actualWeightKg.toInt()} kg"
                            } else {
                                "${uiState.actualWeightKg} kg"
                            }

                            Text(
                                text = weightLabel,
                                style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                                color = GymFlowTheme.colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GymFlowTheme.colors.surfaceVariant)
                                    .clickable { onIncrementWeight() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Weight",
                                    tint = GymFlowTheme.colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Completed Sets Log Card: Confirms completed sets recorded with reps and weight
            if (uiState.completedSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LOGGED SETS (${uiState.completedSets.size} OF ${uiState.totalSets})",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Text(
                                text = "Recorded",
                                style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.Bold),
                                color = GymFlowGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        uiState.completedSets.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(GymFlowGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = GymFlowGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Set ${record.setNumber}",
                                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                }

                                val setWeightStr = if (record.weightKg % 1f == 0f) {
                                    "${record.weightKg.toInt()} kg"
                                } else {
                                    "${record.weightKg} kg"
                                }

                                Text(
                                    text = "${record.reps} reps · $setWeightStr",
                                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GymFlowTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
