package com.gymflow.feature.trainer.plans

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.feature.member.exercises.ExerciseVisuals
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerTextField
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun ExerciseAssignmentScreen(
    dayOfWeek: String,
    planId: String,
    onBackClick: () -> Unit,
    onAddExerciseFromLibrary: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val day = uiState.days[dayOfWeek]
    val exercises = day?.exercises ?: emptyList()
    var previewExercise by remember { mutableStateOf<WorkoutExercise?>(null) }

    val restOptions = listOf(30, 45, 60, 90, 120)

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "$dayOfWeek Routine",
                    subtitle = day?.title ?: "Exercise Assignment",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TrainerDarkBg)
                        .border(1.dp, TrainerDarkBorder)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    TrainerButton(
                        text = "Save Day Exercises",
                        onClick = onSaved,
                        style = TrainerButtonStyle.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Routine Day Title Input
                TrainerTextField(
                    value = day?.title ?: "",
                    onValueChange = { viewModel.updateDayTitle(dayOfWeek, it) },
                    label = "Day Routine Focus",
                    placeholder = "e.g. Chest + Triceps"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Exercises Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assigned Exercises (${exercises.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrainerDarkTextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF27272A))
                            .clickable(onClick = onAddExerciseFromLibrary)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Exercise",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Exercise Editor Cards
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    exercises.forEachIndexed { index, exercise ->
                        TrainerCard(
                            backgroundColor = TrainerDarkCard,
                            borderColor = TrainerDarkBorder,
                            contentPadding = 16.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Header: Thumbnail + Name + Category + Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val gifUrl = ExerciseVisuals.getGifUrl(exercise.exerciseId)
                                            ?: ExerciseVisuals.getGifUrl(exercise.exerciseName)
                                            ?: ExerciseVisuals.getImageUrl(exercise.exerciseId)
                                            ?: ExerciseVisuals.getImageUrl(exercise.exerciseName)

                                        // Animated Exercise GIF / Visual Thumbnail
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(10.dp))
                                                .clickable { previewExercise = exercise },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!gifUrl.isNullOrBlank()) {
                                                SubcomposeAsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(gifUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = exercise.exerciseName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                    loading = {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(18.dp),
                                                                strokeWidth = 2.dp,
                                                                color = Color(0xFF4ADE80)
                                                            )
                                                        }
                                                    },
                                                    error = {
                                                        Icon(
                                                            imageVector = Icons.Default.FitnessCenter,
                                                            contentDescription = null,
                                                            tint = Color(0xFF71717A),
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = Color(0xFF71717A),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF27272A)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4ADE80)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = exercise.exerciseName,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TrainerDarkTextPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = exercise.category,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF4ADE80)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeExercise(dayOfWeek, index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove exercise",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Sets x Reps x Weight Steppers Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sets
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141416))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("SETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (exercise.targetSets > 1) {
                                                        viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetSets = exercise.targetSets - 1))
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                            Text(
                                                text = "${exercise.targetSets}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrainerDarkTextPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetSets = exercise.targetSets + 1))
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    // Reps
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141416))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("REPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (exercise.targetReps > 1) {
                                                        viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetReps = exercise.targetReps - 1))
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                            Text(
                                                text = "${exercise.targetReps}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrainerDarkTextPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetReps = exercise.targetReps + 1))
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    // Weight
                                    Column(
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141416))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("WEIGHT (KG)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (exercise.targetWeightKg >= 2.5f) {
                                                        viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetWeightKg = exercise.targetWeightKg - 2.5f))
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                            Text(
                                                text = "${exercise.targetWeightKg}kg",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4ADE80),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.updateExercise(dayOfWeek, index, exercise.copy(targetWeightKg = exercise.targetWeightKg + 2.5f))
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = TrainerDarkTextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Rest Duration Selector Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = TrainerDarkTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Rest Interval: ${exercise.restDurationSeconds}s",
                                        fontSize = 12.sp,
                                        color = TrainerDarkTextSecondary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        restOptions.forEach { seconds ->
                                            val isSelected = exercise.restDurationSeconds == seconds
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) Color.White else Color(0xFF141416))
                                                    .clickable {
                                                        viewModel.updateExercise(dayOfWeek, index, exercise.copy(restDurationSeconds = seconds))
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "${seconds}s",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.Black else TrainerDarkTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Notes / Coaching cue
                                TrainerTextField(
                                    value = exercise.notes,
                                    onValueChange = {
                                        viewModel.updateExercise(dayOfWeek, index, exercise.copy(notes = it))
                                    },
                                    placeholder = "Technique cue e.g. Keep back flat, full stretch",
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Fullscreen Animated Exercise Demonstration Dialog
        previewExercise?.let { exercise ->
            val fullGifUrl = ExerciseVisuals.getGifUrl(exercise.exerciseId)
                ?: ExerciseVisuals.getGifUrl(exercise.exerciseName)
                ?: ExerciseVisuals.getImageUrl(exercise.exerciseId)
                ?: ExerciseVisuals.getImageUrl(exercise.exerciseName)

            AlertDialog(
                onDismissRequest = { previewExercise = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exercise.exerciseName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrainerDarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = exercise.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80)
                            )
                        }
                        IconButton(onClick = { previewExercise = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TrainerDarkTextSecondary)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!fullGifUrl.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(fullGifUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = exercise.exerciseName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color(0xFF4ADE80))
                                        }
                                    },
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = Color(0xFF71717A),
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                )
                            }
                        }

                        if (exercise.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF27272A))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "💡 Technique Cue: ${exercise.notes}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE4E4E7)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TrainerButton(
                        text = "Close Preview",
                        onClick = { previewExercise = null },
                        style = TrainerButtonStyle.PRIMARY
                    )
                },
                containerColor = TrainerDarkCard
            )
        }
    }
}
