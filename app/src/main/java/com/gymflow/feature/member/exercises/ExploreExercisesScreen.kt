package com.gymflow.feature.member.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gymflow.domain.model.Exercise
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreExercisesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExerciseDetails: (String) -> Unit,
    viewModel: ExploreExercisesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Explore Exercises",
                            style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                            color = GymFlowTheme.colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Browse muscle groups, form cues & stretching",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GymFlowTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GymFlowTheme.colors.background
                )
            )
        },
        containerColor = GymFlowTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(14.dp)),
                    placeholder = {
                        Text(
                            text = "Search exercises, muscles or stretches...",
                            style = GymFlowTheme.typography.BodyMedium,
                            color = GymFlowTheme.colors.textTertiary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GymFlowTheme.colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = GymFlowTheme.colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GymFlowTheme.colors.surface,
                        unfocusedContainerColor = GymFlowTheme.colors.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = GymFlowBlack
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            // Horizontal Muscle Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MuscleCategory.values().forEach { category ->
                    val isSelected = uiState.selectedCategory == category
                    val chipBg = if (isSelected) GymFlowBlack else GymFlowTheme.colors.surfaceVariant
                    val chipFg = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textSecondary

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .clickable { viewModel.onSelectCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (category == MuscleCategory.STRETCHING) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = chipFg,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = category.displayName,
                            style = GymFlowTheme.typography.Caption.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = chipFg,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Results count badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        uiState.selectedCategory == MuscleCategory.STRETCHING ->
                            "${uiState.stretches.size} mobility & stretching routines"
                        else ->
                            "${uiState.exercises.size} exercises found"
                    },
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = GymFlowTheme.colors.textTertiary
                )
            }

            // Content List
            if (uiState.selectedCategory == MuscleCategory.STRETCHING) {
                // Stretching list
                if (uiState.stretches.isEmpty()) {
                    EmptyExploreState(message = "No stretching exercises match your query.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            val formGuide = ExerciseVisuals.getCategoryFormGuide("STRETCHING")
                            if (formGuide != null) {
                                CategoryFormGuideCard(formGuide = formGuide)
                            }
                        }
                        items(uiState.stretches, key = { it.id }) { stretch ->
                            ExploreStretchCard(stretch = stretch)
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            } else {
                // Exercises list
                if (uiState.exercises.isEmpty()) {
                    EmptyExploreState(message = "No exercises found for '${uiState.selectedCategory.displayName}'.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (uiState.selectedCategory != MuscleCategory.ALL) {
                            item {
                                val formGuide = ExerciseVisuals.getCategoryFormGuide(uiState.selectedCategory.name)
                                if (formGuide != null) {
                                    CategoryFormGuideCard(formGuide = formGuide)
                                }
                            }
                        }
                        items(uiState.exercises, key = { it.id }) { exercise ->
                            ExploreExerciseCard(
                                exercise = exercise,
                                onClick = { onNavigateToExerciseDetails(exercise.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    val stretchesCount = StretchingCatalog.getStretchesForExercise(exercise.name + " " + exercise.category).size

    GymFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Thumbnail with GIF/PNG preview
            ExerciseThumbnail(
                imageUrl = exercise.gifUrl ?: exercise.mediaUrl ?: ExerciseVisuals.getImageUrl(exercise.id),
                category = exercise.category,
                equipment = exercise.equipment,
                preferGif = true,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = GymFlowTheme.typography.BodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
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
                    // Category pill
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

                    // Equipment pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GymFlowTheme.colors.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = exercise.equipment,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = GymFlowTheme.colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Stretch indicator if available
                    if (stretchesCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEDE9FE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$stretchesCount Stretches",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                val subInfo = if (exercise.muscleGroup.isNotBlank()) exercise.muscleGroup else exercise.category
                Text(
                    text = subInfo,
                    style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                    color = GymFlowTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = GymFlowTheme.colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExploreStretchCard(
    stretch: StretchingExercise
) {
    val (timingBg, timingFg) = when (stretch.timing) {
        StretchTiming.PRE_WORKOUT_DYNAMIC -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        StretchTiming.POST_WORKOUT_STATIC -> Color(0xFFCCFBF1) to Color(0xFF0F766E)
        StretchTiming.DAILY_MOBILITY -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
    }

    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timing badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(timingBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stretch.timing.label.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = timingFg,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Hold duration pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stretch.recommendedDurationSeconds}s hold",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowTheme.colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If stretch has GIF, render animated preview thumbnail!
                if (!stretch.gifUrl.isNullOrBlank()) {
                    ExerciseThumbnail(
                        imageUrl = stretch.gifUrl,
                        category = stretch.muscleGroup,
                        equipment = "Bodyweight",
                        preferGif = true,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stretch.name,
                        style = GymFlowTheme.typography.Heading2.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = GymFlowTheme.colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Target: ${stretch.targetMuscle} (${stretch.muscleGroup})",
                        style = GymFlowTheme.typography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = GymFlowTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions summary
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                stretch.instructions.take(3).forEachIndexed { index, step ->
                    Text(
                        text = "${index + 1}. $step",
                        style = GymFlowTheme.typography.Caption.copy(fontSize = 12.sp),
                        color = GymFlowTheme.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Breathing cue badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Breathing: ${stretch.breathingCue}",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = GymFlowTheme.colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyExploreState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GymFlowTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = GymFlowTheme.colors.textSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = GymFlowTheme.typography.BodyMedium,
                color = GymFlowTheme.colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryFormGuideCard(
    formGuide: MuscleCategoryFormInfo
) {
    GymFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymFlowTheme.colors.surfaceVariant.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(formGuide.formGifUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${formGuide.title} Form Guide",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = GymFlowBlack
                            )
                        }
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GymFlowTheme.colors.textSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "FORM",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GymFlowGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${formGuide.title.uppercase()} FORM GUIDE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowGreen,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formGuide.targetMuscleLabel,
                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Demonstrating biomechanical form & target muscle recruitment",
                    style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                    color = GymFlowTheme.colors.textSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

