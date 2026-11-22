package com.gymflow.feature.trainer.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.domain.model.Exercise
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
fun TrainerExerciseLibraryScreen(
    isSelectionMode: Boolean = false,
    onBackClick: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TrainerExerciseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = if (isSelectionMode) "Select Exercise" else "Exercise Library",
                    subtitle = "${uiState.filteredExercises.size} Exercises Available",
                    onBackClick = onBackClick
                )
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                TrainerTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search by exercise name, target muscle...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TrainerDarkTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Filter Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.categories.forEach { category ->
                        val isSelected = uiState.selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) Color.White else TrainerDarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else TrainerDarkBorder,
                                    RoundedCornerShape(9999.dp)
                                )
                                .clickable { viewModel.onCategorySelected(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TrainerDarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equipment Filter Chips (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.equipmentList.forEach { equipment ->
                        val isSelected = uiState.selectedEquipment == equipment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF27272A) else Color(0xFF141416))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF4ADE80) else TrainerDarkBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onEquipmentSelected(equipment) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = equipment,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF4ADE80) else TrainerDarkTextTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Exercises List
                if (uiState.filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises match selected filters",
                            color = TrainerDarkTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredExercises, key = { it.id }) { exercise ->
                            val gifUrl = ExerciseVisuals.getGifUrl(exercise.id)
                                ?: ExerciseVisuals.getGifUrl(exercise.name)
                                ?: exercise.mediaUrl
                                ?: ExerciseVisuals.getImageUrl(exercise.id)
                                ?: ExerciseVisuals.getImageUrl(exercise.name)

                            TrainerCard(
                                backgroundColor = TrainerDarkCard,
                                borderColor = TrainerDarkBorder,
                                contentPadding = 12.dp,
                                onClick = { viewModel.openExerciseDetails(exercise) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Exercise Animated GIF Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(10.dp))
                                            .clickable { viewModel.openExerciseDetails(exercise) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!gifUrl.isNullOrBlank()) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(gifUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = exercise.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                loading = {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(16.dp),
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

                                    // Content Column
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        // Row 1: Exercise Name + Badges
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = exercise.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrainerDarkTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF27272A))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = exercise.equipment,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFFD4D4D8)
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0x264ADE80))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = exercise.category,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF4ADE80)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        // Row 2: Target Muscles (Standalone row to prevent layout collisions)
                                        Text(
                                            text = exercise.muscleGroup,
                                            fontSize = 12.sp,
                                            color = TrainerDarkTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Row 3: Rest Timer (Left) + Actions (Right)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Rest Time Badge
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF18181B))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = null,
                                                    tint = TrainerDarkTextTertiary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${exercise.defaultRestSeconds}s rest",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TrainerDarkTextTertiary
                                                )
                                            }

                                            // Action Buttons: Details + Select
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Details",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF4ADE80),
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier
                                                        .clickable { viewModel.openExerciseDetails(exercise) }
                                                        .padding(vertical = 2.dp, horizontal = 4.dp)
                                                )

                                                if (isSelectionMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(9999.dp))
                                                            .background(Color.White)
                                                            .clickable { onExerciseSelected(exercise) }
                                                            .padding(horizontal = 14.dp, vertical = 5.dp)
                                                    ) {
                                                        Text(
                                                            text = "Select",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Exercise Details Dialog
        val selectedExercise = uiState.selectedExerciseForDetails
        if (selectedExercise != null) {
            AlertDialog(
                onDismissRequest = { viewModel.closeExerciseDetails() },
                title = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedExercise.name,
                                color = TrainerDarkTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF27272A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedExercise.equipment,
                                    fontSize = 11.sp,
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = selectedExercise.muscleGroup,
                            fontSize = 12.sp,
                            color = TrainerDarkTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val detailGifUrl = ExerciseVisuals.getGifUrl(selectedExercise.id)
                            ?: ExerciseVisuals.getGifUrl(selectedExercise.name)
                            ?: selectedExercise.mediaUrl
                            ?: ExerciseVisuals.getImageUrl(selectedExercise.id)
                            ?: ExerciseVisuals.getImageUrl(selectedExercise.name)

                        // High-Resolution Animated GIF Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!detailGifUrl.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(detailGifUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = selectedExercise.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
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
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color(0xFF71717A),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "EXECUTION GUIDE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedExercise.instructions.forEachIndexed { i, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${i + 1}.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4ADE80)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    color = TrainerDarkTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (isSelectionMode) {
                        TextButton(
                            onClick = {
                                onExerciseSelected(selectedExercise)
                                viewModel.closeExerciseDetails()
                            }
                        ) {
                            Text("Assign to Routine", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { viewModel.closeExerciseDetails() }) {
                            Text("Done", color = Color.White)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeExerciseDetails() }) {
                        Text("Close", color = TrainerDarkTextSecondary)
                    }
                },
                containerColor = TrainerDarkCard,
                textContentColor = TrainerDarkTextSecondary
            )
        }
    }
}
