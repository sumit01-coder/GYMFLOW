package com.gymflow.feature.member.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.domain.model.Exercise
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectorBottomSheet(
    exercises: List<Exercise>,
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise, sets: Int, reps: Int, weightKg: Float) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedEquipment by remember { mutableStateOf("All") }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    var targetSets by remember { mutableIntStateOf(3) }
    var targetReps by remember { mutableIntStateOf(12) }
    var targetWeightKg by remember { mutableFloatStateOf(20f) }

    val categories = listOf("All", "Chest", "Back", "Shoulders", "Biceps", "Triceps", "Legs", "Abs", "Cardio")
    val equipmentList = listOf("All", "Machine", "Cable", "Dumbbell", "Barbell", "Bodyweight", "Resistance Band", "Smith Machine", "Kettlebell")

    val filteredExercises = exercises.filter { ex ->
        val matchesCategory = selectedCategory == "All" || ex.category.equals(selectedCategory, ignoreCase = true)
        val matchesEquipment = selectedEquipment == "All" || ex.equipment.equals(selectedEquipment, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.category.contains(searchQuery, ignoreCase = true) ||
                (ex.machineName?.contains(searchQuery, ignoreCase = true) == true) ||
                ex.muscleGroup.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesEquipment && matchesSearch
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = GymFlowTheme.colors.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Exercise",
                        style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = "Select from gym machines, cables & free weights",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search by machine, exercise or muscle...",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = GymFlowTheme.colors.textSecondary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GymFlowBlack,
                    unfocusedBorderColor = GymFlowBorder,
                    focusedContainerColor = GymFlowTheme.colors.surface,
                    unfocusedContainerColor = GymFlowTheme.colors.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GymFlowBlack else GymFlowTheme.colors.surfaceVariant)
                            .border(1.dp, if (isSelected) GymFlowBlack else GymFlowBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Equipment Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                equipmentList.forEach { eq ->
                    val isSelected = selectedEquipment == eq
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GymFlowTheme.colors.surface else GymFlowTheme.colors.background)
                            .border(1.dp, if (isSelected) GymFlowBlack else GymFlowBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedEquipment = eq }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = eq,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If an exercise is chosen for parameter tweaking
            val activeEx = selectedExercise
            if (activeEx != null) {
                ConfigureExerciseSection(
                    exercise = activeEx,
                    targetSets = targetSets,
                    onSetsChange = { targetSets = it.coerceIn(1, 10) },
                    targetReps = targetReps,
                    onRepsChange = { targetReps = it.coerceIn(1, 100) },
                    targetWeightKg = targetWeightKg,
                    onWeightChange = { targetWeightKg = it.coerceIn(0f, 500f) },
                    onConfirm = {
                        onExerciseSelected(activeEx, targetSets, targetReps, targetWeightKg)
                        selectedExercise = null
                    },
                    onCancel = { selectedExercise = null }
                )
            } else {
                Text(
                    text = "${filteredExercises.size} exercises available",
                    style = GymFlowTheme.typography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = GymFlowTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Exercise List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredExercises.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No exercises found matching filters.",
                                    style = GymFlowTheme.typography.BodyRegular,
                                    color = GymFlowTheme.colors.textSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredExercises, key = { it.id }) { exercise ->
                            ExerciseCatalogItem(
                                exercise = exercise,
                                onClick = {
                                    selectedExercise = exercise
                                    targetSets = exercise.defaultSets
                                    targetReps = exercise.defaultReps
                                    targetWeightKg = exercise.defaultWeightKg
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCatalogItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseThumbnail(
                imageUrl = exercise.gifUrl ?: exercise.mediaUrl ?: ExerciseVisuals.getImageUrl(exercise.id),
                category = exercise.category,
                equipment = exercise.equipment,
                preferGif = true,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MuscleCategoryChip(category = exercise.category)
                    ExerciseEquipmentBadge(
                        equipment = exercise.equipment,
                        machineName = exercise.machineName
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exercise.muscleGroup,
                    style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                    color = GymFlowTheme.colors.textSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GymFlowTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Select",
                    modifier = Modifier.size(18.dp),
                    tint = GymFlowTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun ConfigureExerciseSection(
    exercise: Exercise,
    targetSets: Int,
    onSetsChange: (Int) -> Unit,
    targetReps: Int,
    onRepsChange: (Int) -> Unit,
    targetWeightKg: Float,
    onWeightChange: (Float) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 16.dp,
        borderColor = GymFlowBlack,
        borderWidth = 1.5.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseThumbnail(
                    imageUrl = exercise.mediaUrl ?: ExerciseVisuals.getImageUrl(exercise.id),
                    category = exercise.category,
                    equipment = exercise.equipment,
                    modifier = Modifier.size(52.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = exercise.machineName ?: exercise.equipment,
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                MuscleCategoryChip(category = exercise.category)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GymFlowBorder)

            // Stepper 1: Target Sets
            StepperRow(
                label = "Target Sets",
                valueDisplay = "$targetSets sets",
                onDecrement = { onSetsChange(targetSets - 1) },
                onIncrement = { onSetsChange(targetSets + 1) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper 2: Target Reps
            StepperRow(
                label = "Target Reps",
                valueDisplay = "$targetReps reps",
                onDecrement = { onRepsChange(targetReps - 1) },
                onIncrement = { onRepsChange(targetReps + 1) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper 3: Target Weight (kg)
            StepperRow(
                label = "Target Weight",
                valueDisplay = String.format(Locale.US, "%.1f kg", targetWeightKg),
                onDecrement = { onWeightChange((targetWeightKg - 2.5f).coerceAtLeast(0f)) },
                onIncrement = { onWeightChange(targetWeightKg + 2.5f) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GymFlowBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onCancel)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GymFlowBlack)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = GymFlowWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add to Workout",
                            style = GymFlowTheme.typography.BodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GymFlowWhite
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    valueDisplay: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = GymFlowTheme.typography.BodyMedium,
            color = GymFlowTheme.colors.textPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .clickable(onClick = onDecrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp),
                    tint = GymFlowTheme.colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .width(76.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = valueDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTheme.colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(GymFlowBlack)
                    .clickable(onClick = onIncrement),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp),
                    tint = GymFlowWhite
                )
            }
        }
    }
}

