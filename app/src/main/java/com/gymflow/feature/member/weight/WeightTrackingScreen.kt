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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.components.GymFlowSegmentedControl
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberBtnPrimaryBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeightTrackingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: WeightViewModel = hiltViewModel()
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
                        text = if (state.preWeightKg > 0f) String.format(Locale.US, "%.2f kg", state.preWeightKg) else "-- kg",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.preRecordedTimeStr != null) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MemberTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Logged at ${state.preRecordedTimeStr}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MemberTextSecondary
                            )
                        } else {
                            Text(
                                text = "Before training session",
                                fontSize = 11.sp,
                                color = MemberTextTertiary
                            )
                        }
                    }
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
                        text = when {
                            state.postWeightKg > 0f -> String.format(Locale.US, "%.2f kg", state.postWeightKg)
                            state.hasPreLoggedToday -> "Pending"
                            else -> "-- kg"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.postWeightKg > 0f) MemberTextPrimary else MemberTextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.postRecordedTimeStr != null) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MemberTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Logged at ${state.postRecordedTimeStr}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MemberTextSecondary
                            )
                        } else if (state.hasPreLoggedToday) {
                            Text(
                                text = "Pending post-session log",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD97706)
                            )
                        } else {
                            Text(
                                text = "After training session",
                                fontSize = 11.sp,
                                color = MemberTextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Difference Banner / Acute Deficit Analysis
            if (state.postWeightKg > 0f && state.preWeightKg > 0f) {
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
            } else if (state.hasPreLoggedToday) {
                // Pre-workout recorded, awaiting post-workout weigh in
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pre-Workout Baseline: ${String.format(Locale.US, "%.2f kg", state.preWeightKg)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Logged at ${state.preRecordedTimeStr ?: "today"}. Tap + when you finish your session to record post-workout weight and measure acute fluid loss.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 16.sp
                            )
                        }
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

            if (state.recentLogs.isEmpty()) {
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Weight Logs Recorded Yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MemberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button above to log your pre & post-workout weigh-in to calculate acute fluid loss.",
                            fontSize = 12.sp,
                            color = MemberTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else {
                state.recentLogs.forEach { log ->
                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(log.recordedAtEpochMs))
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val preTime = timeFormat.format(Date(log.recordedAtEpochMs))
                    val postTime = log.postRecordedAtEpochMs?.let { timeFormat.format(Date(it)) }
                        ?: if (log.postWeightKg > 0f) preTime else null

                    GymFlowCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentPadding = 12.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MemberTextPrimary
                                )

                                if (log.postWeightKg > 0f) {
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
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "In Progress",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Pre and Post exact weights with AM/PM timestamps
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Pre: ${String.format(Locale.US, "%.2f", log.preWeightKg)} kg ($preTime)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MemberTextSecondary
                                    )
                                    Text(text = "•", fontSize = 12.sp, color = MemberTextTertiary)
                                    if (log.postWeightKg > 0f && postTime != null) {
                                        Text(
                                            text = "Post: ${String.format(Locale.US, "%.2f", log.postWeightKg)} kg ($postTime)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MemberTextPrimary
                                        )
                                    } else {
                                        Text(
                                            text = "Post: Pending weigh-in",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }
                            }

                            if (log.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.notes,
                                    fontSize = 12.sp,
                                    color = MemberTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Bi-Phasic Weight Logging Dialog
    if (state.isLogDialogOpen) {
        BiPhasicLogWeightDialog(
            hasPreLoggedToday = state.hasPreLoggedToday,
            existingPreWeight = state.preWeightKg,
            preRecordedTimeStr = state.preRecordedTimeStr,
            currentPostFallback = state.postWeightKg,
            onDismiss = { viewModel.closeLogDialog() },
            onConfirmPre = { pre, notes ->
                viewModel.recordPreWeight(pre, notes)
            },
            onConfirmPost = { post, notes ->
                viewModel.recordPostWeight(post, notes)
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
fun BiPhasicLogWeightDialog(
    hasPreLoggedToday: Boolean,
    existingPreWeight: Float,
    preRecordedTimeStr: String?,
    currentPostFallback: Float,
    onDismiss: () -> Unit,
    onConfirmPre: (Float, String) -> Unit,
    onConfirmPost: (Float, String) -> Unit
) {
    val currentTimeStr = remember {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date())
    }

    // Default weight calculations
    val defaultPre = if (existingPreWeight > 0f) existingPreWeight else 70.0f
    val defaultPost = if (currentPostFallback > 0f) currentPostFallback else (defaultPre - 0.35f).coerceAtLeast(20f)

    var preWeight by remember { mutableStateOf(defaultPre) }
    var postWeight by remember { mutableStateOf(defaultPost) }
    var isEditingPreOverride by remember { mutableStateOf(false) }

    val isPostWorkoutMode = hasPreLoggedToday && !isEditingPreOverride
    var notesText by remember { mutableStateOf("") }
    var isManualInput by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }

    fun formatKg(v: Float): String = String.format(Locale.US, "%.2f", v)

    val currentActiveWeight = if (isPostWorkoutMode) postWeight else preWeight

    fun updateActiveWeight(delta: Float) {
        val updated = Math.round((currentActiveWeight + delta) * 100f) / 100f
        val clamped = updated.coerceIn(20.0f, 250.0f)
        if (isPostWorkoutMode) {
            postWeight = clamped
        } else {
            preWeight = clamped
        }
    }

    val deltaKg = Math.round((postWeight - defaultPre) * 100f) / 100f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MemberSurfaceSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = MemberTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isPostWorkoutMode) "Log Post-Workout Weight" else "Log Pre-Workout Weight",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MemberTextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Recorded at $currentTimeStr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextSecondary
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isPostWorkoutMode) {
                    // Pre-workout Baseline Card
                    GymFlowCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFF3F4F6),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PRE-WORKOUT BASELINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MemberTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pre-workout: ${formatKg(defaultPre)} kg logged at ${preRecordedTimeStr ?: "today"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MemberTextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isEditingPreOverride = true }
                                    .background(Color(0xFFE5E7EB))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Edit Pre",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MemberTextPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (hasPreLoggedToday) {
                    // In edit pre mode, allow switching back to post
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Switch back to Post-Workout →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.clickable { isEditingPreOverride = false }
                        )
                    }
                }

                // Interactive Weight Stepper Hub
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPostWorkoutMode) "POST-WORKOUT WEIGHT" else "PRE-WORKOUT WEIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MemberTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isManualInput) {
                            // Stepper Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Decrement -0.1
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MemberSurfaceSubtle)
                                        .clickable { updateActiveWeight(-0.1f) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Minus 0.1 kg",
                                        tint = MemberTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Center Value Display
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            manualText = formatKg(currentActiveWeight)
                                            isManualInput = true
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = formatKg(currentActiveWeight),
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MemberTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "kg",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MemberTextTertiary,
                                        modifier = Modifier.padding(bottom = 5.dp)
                                    )
                                }

                                // Increment +0.1
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MemberSurfaceSubtle)
                                        .clickable { updateActiveWeight(+0.1f) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Plus 0.1 kg",
                                        tint = MemberTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Quick adjustment chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(-1.0f, -0.5f, 0.5f, 1.0f).forEach { step ->
                                    val label = if (step > 0) "+${step}kg" else "${step}kg"
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MemberSurfaceSubtle)
                                            .clickable { updateActiveWeight(step) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MemberTextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Manual entry hint
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        manualText = formatKg(currentActiveWeight)
                                        isManualInput = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MemberTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Type exact weight with keyboard",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MemberTextSecondary
                                )
                            }
                        } else {
                            // Direct manual typing mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GymFlowTextField(
                                    value = manualText,
                                    onValueChange = { manualText = it },
                                    label = "Enter Exact Weight",
                                    suffixText = "kg",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MemberBtnPrimaryBg)
                                        .clickable {
                                            manualText.toFloatOrNull()?.let { typed ->
                                                if (isPostWorkoutMode) postWeight = typed.coerceIn(20f, 250f)
                                                else preWeight = typed.coerceIn(20f, 250f)
                                            }
                                            isManualInput = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm Manual Entry",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isPostWorkoutMode) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Immediate Sweat Loss / Fluid Delta Preview Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (deltaKg < 0) Color(0xFFEFF6FF) else Color(0xFFF3F4F6))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (deltaKg < 0) Icons.Default.WaterDrop else Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = if (deltaKg < 0) Color(0xFF2563EB) else MemberTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val diffText = if (deltaKg > 0) "+${formatKg(deltaKg)}" else "${formatKg(deltaKg)}"
                            Text(
                                text = "Fluid Shift: $diffText kg",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (deltaKg < 0) Color(0xFF1D4ED8) else MemberTextPrimary
                            )
                            if (deltaKg < 0) {
                                val fluidLossMl = abs((deltaKg * 1000).toInt())
                                val replenishTargetMl = (fluidLossMl * 1.25f).toInt()
                                Text(
                                    text = "Sweat loss: ~$fluidLossMl ml · Drink ~$replenishTargetMl ml to restore",
                                    fontSize = 11.sp,
                                    color = Color(0xFF3B82F6)
                                )
                            } else {
                                Text(
                                    text = "Stable fluid balance · No acute sweat deficit",
                                    fontSize = 11.sp,
                                    color = MemberTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Session Notes Input
                GymFlowTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Session Notes (Optional)",
                    placeholder = if (isPostWorkoutMode) "e.g. Heavy sweat, leg day, sauna" else "e.g. Fasted morning weigh-in"
                )
            }
        },
        confirmButton = {
            if (isPostWorkoutMode) {
                GymFlowPrimaryButton(
                    text = "Save Post-Workout (${formatKg(postWeight)} kg at $currentTimeStr)",
                    onClick = {
                        onConfirmPost(postWeight, notesText)
                    }
                )
            } else {
                GymFlowPrimaryButton(
                    text = "Save Pre-Workout (${formatKg(preWeight)} kg at $currentTimeStr)",
                    onClick = {
                        onConfirmPre(preWeight, notesText)
                    }
                )
            }
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
