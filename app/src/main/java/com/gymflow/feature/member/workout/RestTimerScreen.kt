package com.gymflow.feature.member.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.theme.GymFlowPureBlack
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowThemeMode
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.core.theme.OledRestCard

@Composable
fun RestTimerScreen(
    viewModel: RestTimerViewModel,
    onSkipRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-navigate when timer finishes
    LaunchedEffect(uiState.remainingSeconds) {
        if (uiState.remainingSeconds == 0) {
            onSkipRest()
        }
    }

    GymFlowTheme(mode = GymFlowThemeMode.OLED_REST) {
        RestTimerContent(
            uiState = uiState,
            onSkipRest = onSkipRest,
            onAdd15Seconds = viewModel::add15Seconds,
            modifier = modifier
        )
    }
}

@Composable
fun RestTimerContent(
    uiState: RestTimerUiState,
    onSkipRest: () -> Unit,
    onAdd15Seconds: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.progress,
        label = "restProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GymFlowPureBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "REST TIME",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Catch Your Breath",
                    style = GymFlowTheme.typography.Heading2.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GymFlowWhite
                )
            }

            // Center Circular Countdown Ring
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Track Ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(230.dp),
                    color = Color(0xFF27272A),
                    strokeWidth = 8.dp
                )

                // Active Progress Ring
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(230.dp),
                    color = GymFlowWhite,
                    strokeWidth = 8.dp
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.formattedRemainingTime,
                        style = GymFlowTheme.typography.Timer.copy(
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = GymFlowWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "REMAINING",
                        style = GymFlowTheme.typography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFF71717A)
                    )
                }
            }

            // Bottom Section: Next Set Preview & Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Upcoming Set Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(GymFlowShapesTokens.CardMain)
                        .background(OledRestCard)
                        .border(1.dp, Color(0xFF27272A), GymFlowShapesTokens.CardMain)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF27272A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "NEXT SET",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFA1A1AA)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.nextSetText,
                                style = GymFlowTheme.typography.BodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GymFlowWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Controls: +15s and Skip Rest
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GymFlowButton(
                        text = "+15s",
                        onClick = onAdd15Seconds,
                        style = GymFlowButtonStyle.SECONDARY,
                        modifier = Modifier.weight(0.35f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    GymFlowButton(
                        text = "Skip Rest",
                        onClick = onSkipRest,
                        style = GymFlowButtonStyle.OUTLINED,
                        modifier = Modifier.weight(0.65f),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                tint = GymFlowWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
