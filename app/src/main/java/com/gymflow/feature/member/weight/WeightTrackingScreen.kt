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
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import kotlin.math.abs
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
    // Smart default: If 0.0f, baseline to 70.0 kg for realistic adult bodyweight
    val initialPre = if (currentPre > 0f) currentPre else 70.0f
    val initialPost = if (currentPost > 0f) currentPost else Math.max(20f, initialPre - 0.35f)

    var preWeight by remember { mutableStateOf(initialPre) }
    var postWeight by remember { mutableStateOf(initialPost) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Pre-workout, 1 = Post-workout
    var notesText by remember { mutableStateOf("") }
    var isManualInput by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }

    fun formatKg(v: Float): String = String.format(Locale.US, "%.2f", v)

    val currentActiveWeight = if (activeTab == 0) preWeight else postWeight

    fun updateActiveWeight(delta: Float) {
        val updated = Math.round((currentActiveWeight + delta) * 100f) / 100f
        val clamped = updated.coerceIn(20.0f, 250.0f)
        if (activeTab == 0) {
            preWeight = clamped
            // If post has not been modified independently, keep realistic -0.35 kg delta
            if (postWeight == initialPost) {
                postWeight = Math.max(20f, clamped - 0.35f)
            }
        } else {
            postWeight = clamped
        }
    }

    val deltaKg = Math.round((postWeight - preWeight) * 100f) / 100f

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
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = MemberTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Log Workout Weight",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MemberTextPrimary
                    )
                    Text(
                        text = "Adjust with the stepper spinner or type directly",
                        fontSize = 11.sp,
                        color = MemberTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Pre / Post Segmented Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MemberSurfaceSubtle)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Pre Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (activeTab == 0) MemberBtnPrimaryBg else Color.Transparent)
                            .clickable {
                                activeTab = 0
                                isManualInput = false
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PRE-WORKOUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 0) Color.White else MemberTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatKg(preWeight)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeTab == 0) Color.White else MemberTextPrimary
                            )
                        }
                    }

                    // Post Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (activeTab == 1) MemberBtnPrimaryBg else Color.Transparent)
                            .clickable {
                                activeTab = 1
                                isManualInput = false
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "POST-WORKOUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 1) Color.White else MemberTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatKg(postWeight)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (activeTab == 1) Color.White else MemberTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Interactive Weight Stepper Spinner Hub
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (activeTab == 0) "ADJUST PRE-SESSION WEIGHT" else "ADJUST POST-SESSION WEIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MemberTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isManualInput) {
                            // Stepper Controls with Big Number
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

                            // Quick adjustment chip pills
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

                            // Manual entry hint button
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
                                                if (activeTab == 0) preWeight = typed.coerceIn(20f, 250f)
                                                else postWeight = typed.coerceIn(20f, 250f)
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Live Fluid Shift & Deficit Preview Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (deltaKg < 0) Color(0xFFEFF6FF) else Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (deltaKg < 0) Icons.Default.WaterDrop else Icons.Default.Scale,
                        contentDescription = null,
                        tint = if (deltaKg < 0) Color(0xFF2563EB) else MemberTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        val diffText = if (deltaKg > 0) "+${formatKg(deltaKg)}" else "${formatKg(deltaKg)}"
                        Text(
                            text = "Shift: $diffText kg",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deltaKg < 0) Color(0xFF1D4ED8) else MemberTextPrimary
                        )
                        if (deltaKg < 0) {
                            val fluidLossMl = abs((deltaKg * 1000).toInt())
                            val replenishTargetMl = (fluidLossMl * 1.25f).toInt()
                            Text(
                                text = "Lost ~$fluidLossMl ml fluid · Recommended intake: ~$replenishTargetMl ml",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6)
                            )
                        } else {
                            Text(
                                text = "Stable fluid balance · No acute deficit detected",
                                fontSize = 11.sp,
                                color = MemberTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Session Notes Input
                GymFlowTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = "Session Notes (Optional)",
                    placeholder = "e.g. Chest day, sauna, heavy sweat"
                )
            }
        },
        confirmButton = {
            GymFlowPrimaryButton(
                text = "Save (${formatKg(preWeight)} → ${formatKg(postWeight)} kg)",
                onClick = {
                    onConfirm(preWeight, postWeight, notesText)
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
