package com.gymflow.feature.trainer.progress

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerKpiCard
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
fun TrainerMemberProgressScreen(
    memberId: String,
    onBackClick: () -> Unit,
    onEditPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerMemberProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val timeframes = listOf(
        ProgressTimeframe.D7 to "7D",
        ProgressTimeframe.D30 to "30D",
        ProgressTimeframe.M3 to "3M",
        ProgressTimeframe.M6 to "6M",
        ProgressTimeframe.Y1 to "1Y"
    )

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Member Progress",
                    subtitle = uiState.memberName,
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TrainerDarkBg)
                        .border(1.dp, TrainerDarkBorder)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    TrainerButton(
                        text = "Adjust Routine Plan",
                        onClick = onEditPlanClick,
                        style = TrainerButtonStyle.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Timeframe Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timeframes.forEach { (tf, label) ->
                        val isSelected = uiState.selectedTimeframe == tf
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else TrainerDarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else TrainerDarkBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectTimeframe(tf) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TrainerDarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Weight Progress Card with Canvas Line Chart
                TrainerCard(contentPadding = 16.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WEIGHT TREND",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrainerDarkTextTertiary
                                )
                                Text(
                                    text = "${uiState.currentWeightKg} kg",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrainerDarkTextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x264ADE80))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-2.2 kg (Healthy Fat Loss)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Canvas Line Graph
                        val points = uiState.weightTrendPoints
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val width = size.width
                            val height = size.height

                            val minWeight = 61.5f
                            val maxWeight = 64.5f

                            // Draw subtle horizontal grid lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = height * (i.toFloat() / gridLines)
                                drawLine(
                                    color = Color(0xFF27272A),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                            }

                            if (points.size >= 2) {
                                val path = Path()
                                val fillPath = Path()

                                val stepX = width / (points.size - 1)

                                points.forEachIndexed { i, p ->
                                    val normY = (p.weightKg - minWeight) / (maxWeight - minWeight)
                                    val y = height - (normY * height)
                                    val x = i * stepX

                                    if (i == 0) {
                                        path.moveTo(x, y)
                                        fillPath.moveTo(x, height)
                                        fillPath.lineTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                        fillPath.lineTo(x, y)
                                    }

                                    if (i == points.size - 1) {
                                        fillPath.lineTo(x, height)
                                        fillPath.close()
                                    }
                                }

                                // Fill under curve
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF4ADE80).copy(alpha = 0.35f),
                                            Color(0xFF4ADE80).copy(alpha = 0.0f)
                                        )
                                    )
                                )

                                // Stroke line
                                drawPath(
                                    path = path,
                                    color = Color(0xFF4ADE80),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw glowing dots
                                points.forEachIndexed { i, p ->
                                    val normY = (p.weightKg - minWeight) / (maxWeight - minWeight)
                                    val y = height - (normY * height)
                                    val x = i * stepX

                                    // Outer ring
                                    drawCircle(
                                        color = Color(0xFF09090B),
                                        radius = 6.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                    drawCircle(
                                        color = Color(0xFF4ADE80),
                                        radius = 4.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // X-Axis Date Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            points.forEach { p ->
                                Text(
                                    text = p.dateLabel,
                                    fontSize = 11.sp,
                                    color = TrainerDarkTextTertiary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Performance Metrics Grid (2x2)
                Text(
                    text = "Performance Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerKpiCard(
                        title = "TOTAL WORKOUTS",
                        value = "${uiState.totalWorkouts}",
                        subtitle = "Target: 14 / month",
                        icon = Icons.Default.FitnessCenter,
                        accentColor = TrainerDarkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TrainerKpiCard(
                        title = "TOTAL SETS",
                        value = "${uiState.totalSets}",
                        subtitle = "Volume: 42,800 kg",
                        icon = Icons.Default.Repeat,
                        accentColor = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerKpiCard(
                        title = "CALORIES BURNED",
                        value = "${uiState.totalCalories}",
                        subtitle = "Avg 320 kcal / session",
                        icon = Icons.Default.LocalFireDepartment,
                        accentColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                    TrainerKpiCard(
                        title = "BEST BENCH PRESS",
                        value = "${uiState.bestBenchPressKg.toInt()} kg",
                        subtitle = "+5kg Personal Record",
                        icon = Icons.Default.Verified,
                        accentColor = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Sessions Verified by Coach
                Text(
                    text = "Recent Sessions Verification",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.recentSessions.forEach { session ->
                        TrainerCard(contentPadding = 14.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = session.workoutName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrainerDarkTextPrimary
                                    )
                                    Text(
                                        text = "${session.sessionDate} · ${session.durationMinutes} min · ${session.totalVolumeKg.toInt()}kg volume",
                                        fontSize = 12.sp,
                                        color = TrainerDarkTextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approved", fontSize = 11.sp, color = Color(0xFF4ADE80))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
