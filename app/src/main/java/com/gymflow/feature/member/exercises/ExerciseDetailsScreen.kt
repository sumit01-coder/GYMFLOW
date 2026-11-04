package com.gymflow.feature.member.exercises

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@Composable
fun ExerciseDetailsScreen(
    viewModel: ExerciseDetailsViewModel,
    onNavigateBack: () -> Unit,
    onStartSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ExerciseDetailsContent(
        uiState = uiState,
        onSelectTab = viewModel::onSelectTab,
        onToggleMediaMode = viewModel::toggleMediaMode,
        onNavigateBack = onNavigateBack,
        onStartSet = { onStartSet(uiState.exerciseId) },
        modifier = modifier
    )
}

@Composable
fun ExerciseDetailsContent(
    uiState: ExerciseDetailsUiState,
    onSelectTab: (ExerciseTab) -> Unit,
    onToggleMediaMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onStartSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GymFlowTheme.colors.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GymFlowButton(
                    text = "Start Set 1",
                    onClick = onStartSet,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = GymFlowWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
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
            // Header: Back arrow, title, metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = uiState.exerciseName,
                        style = GymFlowTheme.typography.Heading1.copy(fontSize = 24.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = uiState.metadataSubhead,
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Media Container (Demonstration Animated GIF & HD Photo)
            val isGifActive = uiState.isPlayingGif && !uiState.gifUrl.isNullOrBlank()
            val demonstrationUrl = if (isGifActive) {
                uiState.gifUrl
            } else {
                uiState.mediaUrl ?: ExerciseVisuals.getImageUrl(uiState.exerciseId)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(GymFlowShapesTokens.CardMain)
                    .background(Color(0xFF1E1E22))
                    .border(1.dp, GymFlowBorder, GymFlowShapesTokens.CardMain)
                    .clickable(onClick = onToggleMediaMode),
                contentAlignment = Alignment.Center
            ) {
                if (!demonstrationUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(demonstrationUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = uiState.exerciseName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(GymFlowShapesTokens.CardMain),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = GymFlowWhite.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        },
                        error = {
                            if (isGifActive && !uiState.mediaUrl.isNullOrBlank()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(uiState.mediaUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = uiState.exerciseName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = GymFlowWhite.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Scrim gradient at the bottom for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // Overlay bottom bar with play/pause indicator and GIF mode badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GymFlowWhite.copy(alpha = 0.95f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGifActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isGifActive) "Pause GIF" else "Play GIF",
                                    tint = GymFlowBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isGifActive) "Live Movement Loop" else uiState.videoTitle,
                                style = GymFlowTheme.typography.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = GymFlowWhite,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isGifActive) GymFlowGreen else GymFlowBlack.copy(alpha = 0.8f))
                                .border(0.5.dp, GymFlowWhite.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable(onClick = onToggleMediaMode)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isGifActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(GymFlowWhite, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                }
                                Text(
                                    text = if (isGifActive) "GIF DEMO" else "VIEW GIF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GymFlowWhite.copy(alpha = 0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.videoTitle,
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented Tabs: [Instructions] | [Muscle Info]
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Instructions Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(ExerciseTab.INSTRUCTIONS) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Instructions",
                        style = GymFlowTheme.typography.BodyMedium.copy(
                            fontWeight = if (uiState.selectedTab == ExerciseTab.INSTRUCTIONS) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (uiState.selectedTab == ExerciseTab.INSTRUCTIONS) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (uiState.selectedTab == ExerciseTab.INSTRUCTIONS) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth(0.6f)
                                .background(GymFlowBlack)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                // Muscle Info Tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(ExerciseTab.MUSCLE_INFO) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Muscle Info",
                        style = GymFlowTheme.typography.BodyMedium.copy(
                            fontWeight = if (uiState.selectedTab == ExerciseTab.MUSCLE_INFO) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (uiState.selectedTab == ExerciseTab.MUSCLE_INFO) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (uiState.selectedTab == ExerciseTab.MUSCLE_INFO) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth(0.6f)
                                .background(GymFlowBlack)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            HorizontalDivider(color = GymFlowTheme.colors.border)

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Content
            when (uiState.selectedTab) {
                ExerciseTab.INSTRUCTIONS -> {
                    Text(
                        text = "Step-by-Step Guide",
                        style = GymFlowTheme.typography.Heading2,
                        color = GymFlowTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    uiState.instructions.forEachIndexed { index, step ->
                        StepItem(stepNumber = index + 1, instruction = step)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pro tip card
                    GymFlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GymFlowTheme.colors.surfaceVariant
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GymFlowTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Coach Tip",
                                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GymFlowTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.coachTip,
                                    style = GymFlowTheme.typography.Caption,
                                    color = GymFlowTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }

                ExerciseTab.MUSCLE_INFO -> {
                    Text(
                        text = "Muscle Activation & Gear",
                        style = GymFlowTheme.typography.Heading2,
                        color = GymFlowTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    GymFlowCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "PRIMARY MUSCLE",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.primaryMuscle,
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = GymFlowTheme.colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "SECONDARY SYNERGISTS",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.secondaryMuscles.joinToString(", "),
                                style = GymFlowTheme.typography.BodyMedium,
                                color = GymFlowTheme.colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "EQUIPMENT",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.equipmentRequired,
                                style = GymFlowTheme.typography.BodyMedium,
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    instruction: String
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(GymFlowBlack, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        color = GymFlowWhite
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = instruction,
                style = GymFlowTheme.typography.BodyRegular,
                color = GymFlowTheme.colors.textPrimary
            )
        }
    }
}
