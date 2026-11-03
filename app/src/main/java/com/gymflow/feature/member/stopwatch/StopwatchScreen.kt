package com.gymflow.feature.member.stopwatch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary

@Composable
fun StopwatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: StopwatchViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Stopwatch",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Hero Timer Ring & Digital Readout
            StopwatchDial(state = state)

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            StopwatchActionControls(
                isRunning = state.isRunning,
                elapsedMillis = state.elapsedMillis,
                onStart = { viewModel.start() },
                onPause = { viewModel.pause() },
                onReset = { viewModel.reset() },
                onLap = { viewModel.recordLap() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Interval Presets
            QuickIntervalPresets(
                onSelectPreset = { seconds -> viewModel.setQuickPreset(seconds) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Laps Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MemberTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Laps & Splits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MemberTextPrimary
                    )
                }

                if (state.laps.isNotEmpty()) {
                    Text(
                        text = "${state.laps.size} recorded",
                        fontSize = 12.sp,
                        color = MemberTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Laps List or Empty State
            if (state.laps.isEmpty()) {
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp,
                    backgroundColor = MemberSurfaceSubtle
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MemberTextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Laps Recorded",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MemberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap 'Lap' while running to capture split times.",
                            fontSize = 12.sp,
                            color = MemberTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LapsTable(laps = state.laps)
            }
        }
    }
}

@Composable
private fun StopwatchDial(state: StopwatchUiState) {
    val secondsFraction = (state.elapsedMillis % 60000L).toFloat() / 60000f
    val animatedSweep by animateFloatAsState(
        targetValue = secondsFraction * 360f,
        animationSpec = tween(durationMillis = 50),
        label = "sweepAngle"
    )

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()

            // Background Track Ring
            drawCircle(
                color = Color(0xFFF3F4F6),
                style = Stroke(width = strokeWidth)
            )

            // Animated Progress Sweep Ring
            if (state.elapsedMillis > 0L) {
                drawArc(
                    color = if (state.isRunning) Color(0xFF111111) else Color(0xFFF97316),
                    startAngle = -90f,
                    sweepAngle = if (animatedSweep == 0f && state.elapsedMillis > 0) 360f else animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Time Digits: MM:SS.CC
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.minutesPart,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MemberTextPrimary
                )
                Text(
                    text = ":",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MemberTextPrimary
                )
                Text(
                    text = state.secondsPart,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MemberTextPrimary
                )
                Text(
                    text = ".",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MemberTextSecondary
                )
                Text(
                    text = state.hundredthsPart,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MemberTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            val statusLabel = when {
                state.isRunning -> "RUNNING"
                state.elapsedMillis > 0 -> "PAUSED"
                else -> "READY"
            }
            val statusColor = when {
                state.isRunning -> GymFlowGreen
                state.elapsedMillis > 0 -> Color(0xFFF97316)
                else -> MemberTextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun StopwatchActionControls(
    isRunning: Boolean,
    elapsedMillis: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit
) {
    if (!isRunning && elapsedMillis == 0L) {
        // Initial Ready State: Single Primary Start Button
        GymFlowButton(
            text = "Start",
            onClick = onStart,
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
    } else if (isRunning) {
        // Running State: Lap (Outlined) & Pause (Solid Dark)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GymFlowButton(
                text = "Lap",
                onClick = onLap,
                style = GymFlowButtonStyle.OUTLINED,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            GymFlowButton(
                text = "Pause",
                onClick = onPause,
                style = GymFlowButtonStyle.PRIMARY,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = GymFlowWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    } else {
        // Paused State: Reset (Outlined) & Resume (Primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GymFlowButton(
                text = "Reset",
                onClick = onReset,
                style = GymFlowButtonStyle.DESTRUCTIVE,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = GymFlowWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            GymFlowButton(
                text = "Resume",
                onClick = onStart,
                style = GymFlowButtonStyle.PRIMARY,
                modifier = Modifier.weight(1f),
                leadingIcon = {
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
}

@Composable
private fun QuickIntervalPresets(
    onSelectPreset: (Int) -> Unit
) {
    val presets = listOf(
        30 to "30s Rest",
        45 to "45s Rest",
        60 to "60s Rest",
        90 to "90s Heavy",
        120 to "2m Interval",
        180 to "3m Round"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "QUICK GYM PRESETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MemberTextSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { (seconds, label) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MemberSurfaceSubtle)
                        .border(1.dp, MemberCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectPreset(seconds) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MemberTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LapsTable(laps: List<StopwatchLap>) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 0.dp
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MemberSurfaceSubtle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LAP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "SPLIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = "TOTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.5f)
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = MemberCardBorder)

        // Laps rows
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height((laps.size * 42).coerceAtMost(220).dp)
        ) {
            items(laps) { lap ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap ${lap.lapNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextPrimary
                        )
                        if (lap.isBest) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "FAST",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A36A)
                                )
                            }
                        } else if (lap.isSlowest) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SLOW",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Text(
                        text = "+${lap.formattedSplit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (lap.isBest) GymFlowGreen else MemberTextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.5f)
                    )

                    Text(
                        text = lap.formattedTotal,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MemberTextSecondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = MemberCardBorder)
            }
        }
    }
}

