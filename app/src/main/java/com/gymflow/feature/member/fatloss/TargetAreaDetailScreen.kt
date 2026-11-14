package com.gymflow.feature.member.fatloss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowGreenLight
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import kotlinx.coroutines.launch

@Composable
fun TargetAreaDetailScreen(
    zoneId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    val zone = remember(zoneId) { TargetFatLossCatalog.getZoneById(zoneId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: HIIT, 1: Firming, 2: Nutrition

    if (zone == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GymFlowTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Target area not found", style = GymFlowTheme.typography.BodyRegular)
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = GymFlowTheme.colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "TARGET BODY COMPOSITION",
                        style = GymFlowTheme.typography.Caption.copy(
                            color = GymFlowGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = zone.name,
                        style = GymFlowTheme.typography.Heading1.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Anatomy Highlight & Timeline Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GymFlowTheme.colors.surface)
                        .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REALISTIC TIMELINE",
                                style = GymFlowTheme.typography.Caption.copy(
                                    color = GymFlowGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${zone.realisticTimelineWeeks} Weeks Protocol",
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GymFlowTheme.colors.surface)
                        .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = GymFlowGreenLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEFICIT GOAL",
                                style = GymFlowTheme.typography.Caption.copy(
                                    color = GymFlowGreenLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "-${zone.recommendedCaloricDeficitKcal} kcal / day",
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Hero Target Zone Animated Form GIF Banner
            if (!zone.heroGifUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(zone.heroGifUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = zone.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = GymFlowTheme.colors.textSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )

                    // Subtle gradient overlay with Target Muscle label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Zone Kinetic Form",
                                style = GymFlowTheme.typography.Caption.copy(
                                    color = GymFlowWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GymFlowGreen)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "LIVE MOTION",
                                    style = GymFlowTheme.typography.Caption.copy(
                                        color = GymFlowBlack,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Science & Physiology Blueprint Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GymFlowTheme.colors.surface)
                    .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GymFlowGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ANATOMY & FAT LOSS SCIENCE",
                                style = GymFlowTheme.typography.Caption.copy(
                                    color = GymFlowGreen,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = zone.anatomyHighlight,
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = zone.scienceOverview,
                        style = GymFlowTheme.typography.BodyRegular.copy(
                            color = GymFlowTheme.colors.textSecondary,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(4.dp)
            ) {
                TargetTabButton(
                    title = "🔥 HIIT Circuit",
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TargetTabButton(
                    title = "🏋️ Toning",
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
                TargetTabButton(
                    title = "🥗 Nutrition",
                    isSelected = selectedTab == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab 0: HIIT Circuit
            AnimatedVisibility(visible = selectedTab == 0, enter = fadeIn(), exit = fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = zone.hiitBurnerTitle,
                            style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GymFlowGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${zone.hiitBurnerDurationMin} MIN",
                                style = GymFlowTheme.typography.Caption.copy(
                                    color = GymFlowGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    zone.hiitCircuitExercises.forEachIndexed { index, exercise ->
                        ExerciseRowItem(index = index + 1, exercise = exercise)
                    }
                }
            }

            // Tab 1: Targeted Firming Routines
            AnimatedVisibility(visible = selectedTab == 1, enter = fadeIn(), exit = fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Targeted Hypertrophy & Muscle Tightening",
                        style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold)
                    )

                    zone.firmingRoutineExercises.forEachIndexed { index, exercise ->
                        ExerciseRowItem(index = index + 1, exercise = exercise)
                    }
                }
            }

            // Tab 2: Nutrition & Deficit Guide
            AnimatedVisibility(visible = selectedTab == 2, enter = fadeIn(), exit = fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GymFlowGreen.copy(alpha = 0.1f))
                            .border(1.dp, GymFlowGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Target Caloric Deficit",
                                    style = GymFlowTheme.typography.Caption.copy(color = GymFlowGreen, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Maintain -${zone.recommendedCaloricDeficitKcal} kcal below maintenance",
                                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    zone.nutritionKeyAdvice.forEachIndexed { index, tip ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GymFlowTheme.colors.surface)
                                .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(GymFlowTheme.colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = GymFlowTheme.typography.Caption.copy(
                                            color = GymFlowGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tip,
                                    style = GymFlowTheme.typography.BodyRegular.copy(lineHeight = 20.sp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button: Schedule This Routine
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Target routine added to your training focus!")
                    }
                    onNavigateToSchedule()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymFlowBlack,
                    contentColor = GymFlowWhite
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SCHEDULE THIS TARGET ROUTINE",
                    style = GymFlowTheme.typography.Button.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TargetTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) GymFlowBlack else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = GymFlowTheme.typography.Caption.copy(
                color = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ExerciseRowItem(
    index: Int,
    exercise: TargetZoneExercise
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise Animated GIF / Thumbnail or Index badge
                if (!exercise.gifUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GymFlowTheme.colors.surfaceVariant)
                            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(exercise.gifUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = exercise.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$index",
                                        style = GymFlowTheme.typography.Caption.copy(
                                            color = GymFlowGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GymFlowGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = GymFlowTheme.typography.Caption.copy(
                                color = GymFlowGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exercise.name,
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exercise.repsOrDuration,
                            style = GymFlowTheme.typography.Caption.copy(
                                color = GymFlowGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = exercise.targetMuscle,
                        style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exercise.instruction,
                style = GymFlowTheme.typography.Caption.copy(
                    color = GymFlowTheme.colors.textSecondary,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
