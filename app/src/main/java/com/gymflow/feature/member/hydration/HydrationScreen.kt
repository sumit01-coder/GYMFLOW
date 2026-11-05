package com.gymflow.feature.member.hydration

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowOutlinedButton
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberBtnPrimaryBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.theme.MemberTextTertiary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HydrationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HydrationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Hydration",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Drag-to-Adjust Circular Water Visual
            InteractiveHydrationVisual(
                currentMl = state.currentIntakeMl,
                goalMl = state.dailyGoalMl,
                onSaveIntake = { targetTotalMl ->
                    viewModel.saveIntake(targetTotalMl)
                },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Fluid Recovery Warning/Banner if post workout deficit exists
            if (state.postWorkoutDeficitMl > 0) {
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFFBBF7D0),
                    contentPadding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Post-Workout Rehydration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "${state.postWorkoutDeficitMl} ml remaining to restore acute sweat loss.",
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Add Action Accelerators (+250, +500, +750)
            Text(
                text = "Quick Log Water",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAddButton(
                    amountMl = 250,
                    onClick = { viewModel.addWater(250) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    amountMl = 500,
                    onClick = { viewModel.addWater(500) },
                    modifier = Modifier.weight(1f)
                )
                QuickAddButton(
                    amountMl = 750,
                    onClick = { viewModel.addWater(750) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            GymFlowOutlinedButton(
                text = "+ Custom Amount",
                onClick = { viewModel.openCustomDialog() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Today's Hydration Logs
            Text(
                text = "Today's Intake History",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (state.todayLogs.isEmpty()) {
                Text(
                    text = "No water logged today yet. Stay hydrated!",
                    fontSize = 13.sp,
                    color = MemberTextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                state.todayLogs.forEach { log ->
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.recordedAtEpochMs))
                    GymFlowCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MemberSurfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalDrink,
                                        contentDescription = null,
                                        tint = MemberTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val sign = if (log.amountMl >= 0) "+" else ""
                                    Text(
                                        text = "$sign${log.amountMl} ml",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MemberTextPrimary
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 12.sp,
                                        color = MemberTextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.deleteLog(log.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete entry",
                                        tint = MemberTextTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.isCustomDialogOpen) {
        CustomWaterDialog(
            onDismiss = { viewModel.closeCustomDialog() },
            onConfirm = { amount -> viewModel.addWater(amount) }
        )
    }
}

/**
 * Interactive gesture-based central hydration visual with drag slider.
 * Dragging upward smoothly increases water level in 50 ml increments.
 * Dragging downward decreases it.
 * Triggers subtle haptic feedback (LocalHapticFeedback) on each 50 ml step change.
 */
@Composable
private fun InteractiveHydrationVisual(
    currentMl: Int,
    goalMl: Int,
    onSaveIntake: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragDisplayMl by remember(currentMl) { mutableStateOf(currentMl) }
    var dragAccumulator by remember { mutableStateOf(0f) }

    val activeMl = if (isDragging) dragDisplayMl else currentMl
    val progress = if (goalMl > 0) (activeMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = if (isDragging) tween(durationMillis = 60) else tween(durationMillis = 600),
        label = "HydrationProgress"
    )

    // Sensitivity: 16 pixels of vertical drag corresponds to 1 increment step of 50 ml
    val pixelsPerStep = 16f

    Box(
        modifier = modifier
            .pointerInput(currentMl) {
                detectVerticalDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragDisplayMl = currentMl
                        dragAccumulator = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        if (dragDisplayMl != currentMl) {
                            onSaveIntake(dragDisplayMl)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        if (dragDisplayMl != currentMl) {
                            onSaveIntake(dragDisplayMl)
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Screen Y goes downwards, so dragging UP has negative dragAmount
                        dragAccumulator += (-dragAmount)

                        if (dragAccumulator >= pixelsPerStep) {
                            val steps = (dragAccumulator / pixelsPerStep).toInt()
                            dragAccumulator %= pixelsPerStep
                            val next = (dragDisplayMl + steps * 50).coerceIn(0, 10000)
                            if (next != dragDisplayMl) {
                                dragDisplayMl = next
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        } else if (dragAccumulator <= -pixelsPerStep) {
                            val steps = ((-dragAccumulator) / pixelsPerStep).toInt()
                            dragAccumulator = -((-dragAccumulator) % pixelsPerStep)
                            val next = (dragDisplayMl - steps * 50).coerceIn(0, 10000)
                            if (next != dragDisplayMl) {
                                dragDisplayMl = next
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 15.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            // Background Track
            drawArc(
                color = if (isDragging) Color(0xFFE5E7EB) else Color(0xFFF3F4F6),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Track
            val activeSweep = 260f * animatedProgress
            drawArc(
                color = if (isDragging) Color(0xFF1E3A8A) else MemberBtnPrimaryBg,
                startAngle = 140f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = if (isDragging) Color(0xFF2563EB) else MemberTextPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.US).format(activeMl)} ml",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MemberTextPrimary
            )
            Text(
                text = "/ ${NumberFormat.getNumberInstance(Locale.US).format(goalMl)} ml goal",
                fontSize = 13.sp,
                color = MemberTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (isDragging) {
                val delta = dragDisplayMl - currentMl
                val deltaSign = if (delta >= 0) "+" else ""
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (delta >= 0) Color(0xFFEFF6FF) else Color(0xFFFEF3C7))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$deltaSign$delta ml (Release to save)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (delta >= 0) Color(0xFF1D4ED8) else Color(0xFFB45309)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(MemberSurfaceSubtle)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% completed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MemberTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = MemberTextTertiary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Drag ↕ to adjust level (50 ml)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MemberTextSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickAddButton(
    amountMl: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MemberSurfaceSubtle)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MemberTextPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$amountMl ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
        }
    }
}

@Composable
private fun CustomWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("300") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Water Intake",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter amount in milliliters (ml):",
                    fontSize = 13.sp,
                    color = MemberTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                GymFlowTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount",
                    suffixText = "ml",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            GymFlowPrimaryButton(
                text = "Add",
                onClick = {
                    val amount = amountText.toIntOrNull() ?: 250
                    onConfirm(amount)
                }
            )
        },
        dismissButton = {
            GymFlowSecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
