package com.gymflow.feature.member.hydration

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HydrationScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HydrationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

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

            // Custom Circular Progress Bar
            HydrationCircularProgress(
                currentMl = state.currentIntakeMl,
                goalMl = state.dailyGoalMl,
                progress = state.progressPercent,
                modifier = Modifier.size(240.dp)
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

            // Quick Add Headline
            Text(
                text = "Quick Log Water",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Action Buttons (+250, +500, +750)
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
                                    Text(
                                        text = "+${log.amountMl} ml",
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

                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
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

@Composable
private fun HydrationCircularProgress(
    currentMl: Int,
    goalMl: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "HydrationProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            // Background Track
            drawArc(
                color = Color(0xFFF3F4F6),
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
                color = MemberBtnPrimaryBg,
                startAngle = 140f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = MemberTextPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.US).format(currentMl)} ml",
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
