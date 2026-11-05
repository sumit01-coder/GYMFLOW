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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowSegmentedControl
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.util.TimerAlertUtils

@Composable
fun StopwatchScreen(
    onNavigateBack: () -> Unit,
    viewModel: StopwatchViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCustomDialog by remember { mutableStateOf(false) }

    // Listen for CountdownFinished single-shot events to trigger haptics and audio alert
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is StopwatchEvent.CountdownFinished -> {
                    TimerAlertUtils.triggerCompletionAlert(context)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = if (state.mode == StopwatchMode.STOPWATCH) "Athletic Stopwatch" else "Countdown Timer",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Mode Segmented Toggle: [Stopwatch] vs [Countdown Timer]
            GymFlowSegmentedControl(
                items = listOf("Stopwatch", "Countdown Timer"),
                selectedIndex = if (state.mode == StopwatchMode.STOPWATCH) 0 else 1,
                onItemSelected = { index ->
                    viewModel.setMode(if (index == 0) StopwatchMode.STOPWATCH else StopwatchMode.COUNTDOWN)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (state.mode) {
                StopwatchMode.STOPWATCH -> {
                    StopwatchModeContent(
                        state = state,
                        onStart = { viewModel.start() },
                        onPause = { viewModel.pause() },
                        onReset = { viewModel.reset() },
                        onLap = { viewModel.recordLap() },
                        onSelectPreset = { seconds -> viewModel.setQuickPreset(seconds) }
                    )
                }

                StopwatchMode.COUNTDOWN -> {
                    CountdownModeContent(
                        state = state,
                        onStartCountdown = { viewModel.startCountdown() },
                        onPauseCountdown = { viewModel.pauseCountdown() },
                        onResetCountdown = { viewModel.resetCountdown() },
                        onSelectPreset = { seconds -> viewModel.selectCountdownPreset(seconds) },
                        onOpenCustomDialog = { showCustomDialog = true }
                    )
                }
            }
        }
    }

    // Custom Timer Stepper Dialog
    if (showCustomDialog) {
        CustomCountdownDialog(
            initialMinutes = state.customMinutes,
            initialSeconds = state.customSeconds,
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes, seconds ->
                viewModel.setCustomCountdown(minutes, seconds)
                showCustomDialog = false
            }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// FORWARD STOPWATCH MODE CONTENT
// -------------------------------------------------------------------------------------------------

@Composable
private fun StopwatchModeContent(
    state: StopwatchUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onSelectPreset: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Forward Dial
        StopwatchDial(state = state)

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        StopwatchActionControls(
            isRunning = state.isRunning,
            elapsedMillis = state.elapsedMillis,
            onStart = onStart,
            onPause = onPause,
            onReset = onReset,
            onLap = onLap
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Quick Presets
        QuickIntervalPresets(onSelectPreset = onSelectPreset)

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

// -------------------------------------------------------------------------------------------------
// REVERSE COUNTDOWN TIMER MODE CONTENT
// -------------------------------------------------------------------------------------------------

@Composable
private fun CountdownModeContent(
    state: StopwatchUiState,
    onStartCountdown: () -> Unit,
    onPauseCountdown: () -> Unit,
    onResetCountdown: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onOpenCustomDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Reverse Circular Dial (100% -> 0%) with Smooth 50fps Display
        CountdownDial(state = state)

        Spacer(modifier = Modifier.height(20.dp))

        // Countdown Action Buttons
        CountdownActionControls(
            isCountdownRunning = state.isCountdownRunning,
            isCountdownFinished = state.isCountdownFinished,
            countdownRemainingMillis = state.countdownRemainingMillis,
            countdownTotalMillis = state.countdownTotalMillis,
            onStart = onStartCountdown,
            onPause = onPauseCountdown,
            onReset = onResetCountdown
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Preset Chips (30s, 45s, 60s, 90s, 2m, 3m, 5m + Custom)
        CountdownPresetChips(
            selectedPreset = state.selectedCountdownPresetSeconds,
            onSelectPreset = onSelectPreset,
            onOpenCustom = onOpenCustomDialog
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Interval Informational Card
        GymFlowCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp,
            backgroundColor = MemberSurfaceSubtle
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Interval & Rest Countdown",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Haptic pulse & tone will sound automatically when timer reaches 00:00.",
                        fontSize = 12.sp,
                        color = MemberTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// -------------------------------------------------------------------------------------------------
// REVERSE COUNTDOWN DIAL
// -------------------------------------------------------------------------------------------------

@Composable
private fun CountdownDial(state: StopwatchUiState) {
    // Reverse countdown progress animates from 100% down to 0%
    val animatedProgress by animateFloatAsState(
        targetValue = state.countdownProgress,
        animationSpec = tween(durationMillis = 50),
        label = "countdownProgress"
    )

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()

            // Background Track Ring
            drawCircle(
                color = Color(0xFFE5E7EB),
                style = Stroke(width = strokeWidth)
            )

            // Reverse Active Ring (100% down to 0%)
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0f) {
                val ringColor = when {
                    state.isCountdownFinished -> Color(0xFFEF4444)
                    state.isCountdownRunning -> GymFlowBlack
                    else -> Color(0xFFF97316)
                }

                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Smooth 50fps countdown display: MM:SS.ss
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.countdownMinutesPart,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.isCountdownFinished) Color(0xFFEF4444) else MemberTextPrimary
                )
                Text(
                    text = ":",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.isCountdownFinished) Color(0xFFEF4444) else MemberTextPrimary
                )
                Text(
                    text = state.countdownSecondsPart,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.isCountdownFinished) Color(0xFFEF4444) else MemberTextPrimary
                )
                Text(
                    text = ".",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.isCountdownFinished) Color(0xFFEF4444) else MemberTextSecondary
                )
                Text(
                    text = state.countdownHundredthsPart,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.isCountdownFinished) Color(0xFFEF4444) else MemberTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status Badge
            val (badgeLabel, badgeColor) = when {
                state.isCountdownFinished -> "TIME'S UP!" to Color(0xFFEF4444)
                state.isCountdownRunning -> "COUNTING DOWN" to GymFlowGreen
                state.countdownRemainingMillis < state.countdownTotalMillis -> "PAUSED" to Color(0xFFF97316)
                else -> "READY" to MemberTextSecondary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = badgeColor
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COUNTDOWN ACTION CONTROLS
// -------------------------------------------------------------------------------------------------

@Composable
private fun CountdownActionControls(
    isCountdownRunning: Boolean,
    isCountdownFinished: Boolean,
    countdownRemainingMillis: Long,
    countdownTotalMillis: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    when {
        isCountdownFinished -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GymFlowButton(
                    text = "Reset",
                    onClick = onReset,
                    style = GymFlowButtonStyle.OUTLINED,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                GymFlowButton(
                    text = "Restart",
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

        isCountdownRunning -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GymFlowButton(
                    text = "Reset",
                    onClick = onReset,
                    style = GymFlowButtonStyle.OUTLINED,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
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
        }

        countdownRemainingMillis < countdownTotalMillis -> {
            // Paused state
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

        else -> {
            // Ready state
            GymFlowButton(
                text = "Start Timer",
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
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PRESET CHIPS (30s, 45s, 60s, 90s, 2m, 3m, 5m + Custom)
// -------------------------------------------------------------------------------------------------

@Composable
private fun CountdownPresetChips(
    selectedPreset: Int?,
    onSelectPreset: (Int) -> Unit,
    onOpenCustom: () -> Unit
) {
    val presets = listOf(
        30 to "30s",
        45 to "45s",
        60 to "60s",
        90 to "90s",
        120 to "2m",
        180 to "3m",
        300 to "5m"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DURATION PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MemberTextSecondary
            )

            Text(
                text = "Tap chip to select",
                fontSize = 11.sp,
                color = MemberTextTertiary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { (seconds, label) ->
                val isSelected = selectedPreset == seconds
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GymFlowBlack else MemberSurfaceSubtle)
                        .border(
                            1.dp,
                            if (isSelected) GymFlowBlack else MemberCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectPreset(seconds) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GymFlowWhite else MemberTextPrimary
                    )
                }
            }

            // Custom Time Chip
            item {
                val isCustomSelected = selectedPreset == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCustomSelected) GymFlowBlack else MemberSurfaceSubtle)
                        .border(
                            1.dp,
                            if (isCustomSelected) GymFlowBlack else MemberCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onOpenCustom() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isCustomSelected) GymFlowWhite else MemberTextPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Custom",
                            fontSize = 13.sp,
                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCustomSelected) GymFlowWhite else MemberTextPrimary
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// CUSTOM COUNTDOWN STEPPER DIALOG
// -------------------------------------------------------------------------------------------------

@Composable
private fun CustomCountdownDialog(
    initialMinutes: Int,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, seconds: Int) -> Unit
) {
    var minutes by remember { mutableIntStateOf(initialMinutes) }
    var seconds by remember { mutableIntStateOf(initialSeconds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Custom Timer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure minutes and seconds for your interval",
                    fontSize = 13.sp,
                    color = MemberTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minutes Stepper
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MINUTES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MemberTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { minutes = (minutes - 1).coerceAtLeast(0) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MemberSurfaceSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus Minute",
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "%02d".format(minutes),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MemberTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { minutes = (minutes + 1).coerceAtMost(59) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MemberSurfaceSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus Minute",
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Divider colon
                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextSecondary,
                        modifier = Modifier.padding(top = 18.dp)
                    )

                    // Seconds Stepper
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SECONDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MemberTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { seconds = (seconds - 5).coerceAtLeast(0) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MemberSurfaceSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minus 5 Seconds",
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = "%02d".format(seconds),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MemberTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            IconButton(
                                onClick = { seconds = (seconds + 5).coerceAtMost(59) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MemberSurfaceSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus 5 Seconds",
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GymFlowButton(
                text = "Apply Timer",
                onClick = {
                    val finalMinutes = minutes
                    val finalSeconds = if (finalMinutes == 0 && seconds == 0) 30 else seconds
                    onConfirm(finalMinutes, finalSeconds)
                },
                style = GymFlowButtonStyle.PRIMARY,
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MemberTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// -------------------------------------------------------------------------------------------------
// FORWARD STOPWATCH COMPONENTS (DIAL, ACTION CONTROLS, PRESETS, LAPS TABLE)
// -------------------------------------------------------------------------------------------------

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
