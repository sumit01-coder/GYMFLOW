package com.gymflow.feature.member.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowSegmentedControl
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberBtnPrimaryBg
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProgressAnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Progress",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Time Range Tabs: 7D, 30D, 3M, 6M, 1Y
            GymFlowSegmentedControl(
                items = TimeRangeTab.values().map { it.label },
                selectedIndex = state.selectedTab.ordinal,
                onItemSelected = { index ->
                    viewModel.selectTimeRange(TimeRangeTab.values()[index])
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Stat Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Workouts",
                    value = state.totalWorkouts.toString(),
                    icon = Icons.Default.FitnessCenter,
                    subtitle = "Sessions completed",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Sets",
                    value = numberFormat.format(state.totalSets),
                    icon = Icons.Default.ViewAgenda,
                    subtitle = "Completed sets",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Reps",
                    value = numberFormat.format(state.totalReps),
                    icon = Icons.Default.Repeat,
                    subtitle = "Repetitions executed",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Calories Burned",
                    value = "${numberFormat.format(state.caloriesBurned)} kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    subtitle = "Estimated energy",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weight Progress Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weight Progress",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MemberTextPrimary
                    )
                    Text(
                        text = "Tracked across selected period",
                        fontSize = 12.sp,
                        color = MemberTextSecondary
                    )
                }

                Text(
                    text = "${state.weightProgressPoints.firstOrNull()?.weightKg ?: 0f} → ${state.weightProgressPoints.lastOrNull()?.weightKg ?: 0f} kg",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MemberTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weight Progress Chart
            ProgressWeightChart(
                points = state.weightProgressPoints,
                minY = state.minY,
                maxY = state.maxY,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Total Volume Banner
            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cumulative Training Volume",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${numberFormat.format(state.totalVolumeKg.toInt())} kg",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MemberTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+12% vs prior",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Records Unlocked
            Text(
                text = "Personal Records (PRs)",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            state.personalRecords.forEach { pr ->
                GymFlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pr.exerciseName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MemberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Best: ${pr.bestSet} · ${pr.achievedDate}",
                                fontSize = 12.sp,
                                color = MemberTextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${pr.oneRepMaxKg} kg",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MemberTextPrimary
                            )
                            Text(
                                text = "Est. 1RM",
                                fontSize = 11.sp,
                                color = MemberTextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    GymFlowCard(
        modifier = modifier,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MemberTextSecondary
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MemberSurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MemberTextPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MemberTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MemberTextTertiary
        )
    }
}

@Composable
fun ProgressWeightChart(
    points: List<ProgressChartPoint>,
    minY: Float,
    maxY: Float,
    modifier: Modifier = Modifier
) {
    GymFlowCard(
        modifier = modifier,
        contentPadding = 16.dp
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingBottom = 28.dp.toPx()
            val paddingLeft = 36.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom
            val yRange = (maxY - minY).coerceAtLeast(0.5f)

            // Horizontal Grid Lines
            val gridStep = yRange / 3f
            for (i in 0..3) {
                val value = minY + (gridStep * i)
                val y = paddingTop + chartHeight - ((value - minY) / yRange * chartHeight)

                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        isAntiAlias = true
                    }
                    drawText(
                        String.format(Locale.US, "%.1f", value),
                        8f,
                        y + 8f,
                        paint
                    )
                }
            }

            // Data coordinates
            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)
            val coords = points.mapIndexed { index, point ->
                val x = paddingLeft + (index * stepX)
                val clamped = point.weightKg.coerceIn(minY, maxY)
                val y = paddingTop + chartHeight - ((clamped - minY) / yRange * chartHeight)
                Offset(x, y)
            }

            // Curve & Fill Paths
            val path = Path()
            val fillPath = Path()

            coords.forEachIndexed { i, pt ->
                if (i == 0) {
                    path.moveTo(pt.x, pt.y)
                    fillPath.moveTo(pt.x, paddingTop + chartHeight)
                    fillPath.lineTo(pt.x, pt.y)
                } else {
                    val prev = coords[i - 1]
                    val controlX1 = prev.x + (pt.x - prev.x) / 2f
                    val controlY1 = prev.y
                    val controlX2 = prev.x + (pt.x - prev.x) / 2f
                    val controlY2 = pt.y
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, pt.x, pt.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, pt.x, pt.y)
                }
            }

            fillPath.lineTo(coords.last().x, paddingTop + chartHeight)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x33000000),
                        Color(0x05000000),
                        Color.Transparent
                    ),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )

            drawPath(
                path = path,
                color = MemberBtnPrimaryBg,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw Point Dots & X-axis Labels
            coords.forEachIndexed { i, pt ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = MemberBtnPrimaryBg,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(
                        points[i].label,
                        pt.x,
                        height - 4f,
                        paint
                    )
                }
            }
        }
    }
}
