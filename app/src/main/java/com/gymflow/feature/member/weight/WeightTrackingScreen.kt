package com.gymflow.feature.member.weight

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.components.GymFlowSegmentedControl
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberBtnPrimaryBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurface
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.theme.StatusActive
import com.gymflow.core.theme.StatusDestructive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeightTrackingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: WeightViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Weight Tracking",
            onBackClick = onNavigateBack,
            actions = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MemberSurfaceSubtle)
                        .clickable { viewModel.openLogDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Weight",
                        tint = MemberTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Segmented Filter Toggle
            GymFlowSegmentedControl(
                items = WeightTimeFilter.values().map { it.label },
                selectedIndex = state.selectedFilter.ordinal,
                onItemSelected = { index ->
                    viewModel.onFilterSelected(WeightTimeFilter.values()[index])
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Comparison Cards (2 Columns: Pre vs Post)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pre-workout Card
                GymFlowCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pre-workout",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MemberTextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f kg", state.preWeightKg),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Before training session",
                        fontSize = 11.sp,
                        color = MemberTextTertiary
                    )
                }

                // Post-workout Card
                GymFlowCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Post-workout",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MemberTextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f kg", state.postWeightKg),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "After training session",
                        fontSize = 11.sp,
                        color = MemberTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Difference Banner
            val delta = state.deltaAnalysis.deltaKg
            val isNegative = delta < 0
            val deltaText = if (delta > 0) "+${String.format(Locale.US, "%.2f", delta)} kg"
            else "${String.format(Locale.US, "%.2f", delta)} kg"

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isNegative) Color(0xFFF9FAFB) else MemberSurfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, MemberCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Difference: $deltaText",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MemberTextPrimary
                            )
                        }

                        if (state.deltaAnalysis.fluidLossMl > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-${state.deltaAnalysis.fluidLossMl}ml fluid",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.deltaAnalysis.summaryNote,
                        fontSize = 13.sp,
                        color = MemberTextSecondary,
                        lineHeight = 18.sp
                    )

                    if (state.deltaAnalysis.recommendedFluidIntakeMl > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 ${state.deltaAnalysis.advice}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas-drawn Weight Trend Line Graph
            Text(
                text = "Weight Trend",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your acute changes and multi-day trend",
                fontSize = 13.sp,
                color = MemberTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            WeightTrendChart(
                points = state.trendPoints,
                minY = state.minY,
                maxY = state.maxY,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Logs Section
            Text(
                text = "Recent Sessions",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            state.recentLogs.forEach { log ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.recordedAtEpochMs))
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
                        Column {
                            Text(
                                text = dateStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MemberTextPrimary
                            )
                            if (log.notes.isNotEmpty()) {
                                Text(
                                    text = log.notes,
                                    fontSize = 12.sp,
                                    color = MemberTextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.US, "%.2f", log.preWeightKg)} → ${String.format(Locale.US, "%.2f", log.postWeightKg)} kg",
                                fontSize = 13.sp,
                                color = MemberTextSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val diff = log.deltaKg
                            val diffLabel = if (diff > 0) "+${String.format(Locale.US, "%.2f", diff)}"
                            else String.format(Locale.US, "%.2f", diff)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$diffLabel kg",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MemberTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Quick Log Dialog
    if (state.isLogDialogOpen) {
        QuickLogWeightDialog(
            currentPre = state.preWeightKg,
            currentPost = state.postWeightKg,
            onDismiss = { viewModel.closeLogDialog() },
            onConfirm = { pre, post, notes ->
                viewModel.recordWeight(pre, post, notes)
            }
        )
    }
}

@Composable
fun WeightTrendChart(
    points: List<WeightTrendPoint>,
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

            // Draw horizontal grid lines & Y-axis labels
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

            // Calculate coordinates for points
            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)
            val coords = points.mapIndexed { index, point ->
                val x = paddingLeft + (index * stepX)
                val clamped = point.weightKg.coerceIn(minY, maxY)
                val y = paddingTop + chartHeight - ((clamped - minY) / yRange * chartHeight)
                Offset(x, y)
            }

            // Draw smooth curve path
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

            // Fill gradient under curve
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

            // Draw line
            drawPath(
                path = path,
                color = MemberBtnPrimaryBg,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw circles on data points & X-axis labels
            coords.forEachIndexed { i, pt ->
                // Outer circle
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                // Inner black dot
                drawCircle(
                    color = MemberBtnPrimaryBg,
                    radius = 3.5.dp.toPx(),
                    center = pt
                )

                // Label on X-axis
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(
                        points[i].dayLabel,
                        pt.x,
                        height - 4f,
                        paint
                    )
                }
            }
        }
    }
}

@Composable
fun QuickLogWeightDialog(
    currentPre: Float,
    currentPost: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, String) -> Unit
) {
    var preText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPre)) }
    var postText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPost)) }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Workout Weight",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Record your pre and post session weight to track fluid shifts.",
                    fontSize = 13.sp,
                    color = MemberTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                GymFlowTextField(
                    value = preText,
                    onValueChange = { preText = it },
                    label = "Pre-workout Weight (kg)",
                    suffixText = "kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(10.dp))

                GymFlowTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    label = "Post-workout Weight (kg)",
                    suffixText = "kg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(10.dp))

                GymFlowTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Session Notes (Optional)",
                    placeholder = "e.g. Chest day, sauna"
                )
            }
        },
        confirmButton = {
            GymFlowPrimaryButton(
                text = "Save",
                onClick = {
                    val pre = preText.toFloatOrNull() ?: currentPre
                    val post = postText.toFloatOrNull() ?: currentPost
                    onConfirm(pre, post, notesText)
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
