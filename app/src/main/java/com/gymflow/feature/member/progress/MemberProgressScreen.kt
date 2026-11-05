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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.MetricCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MemberProgressScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            GymFlowBottomBar(
                currentRoute = "member/progress",
                onNavigate = { route ->
                    when (route) {
                        "member/home" -> onNavigateToHome()
                        "member/schedule" -> onNavigateToSchedule()
                        "member/activity" -> onNavigateToActivity()
                        "member/profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Progress & Analytics",
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 26.sp),
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Range Tabs
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TimeRangeTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTimeRange(tab) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeRangeTab.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = GymFlowTheme.colors.primaryButtonBg,
                            activeContentColor = GymFlowTheme.colors.primaryButtonFg,
                            inactiveContainerColor = GymFlowTheme.colors.surfaceVariant,
                            inactiveContentColor = GymFlowTheme.colors.textSecondary
                        )
                    ) {
                        Text(text = tab.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Aggregated Stat Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Total Workouts",
                    value = "${state.totalWorkouts}",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Total Sets",
                    value = "${state.totalSets}",
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Total Reps",
                    value = numberFormat.format(state.totalReps),
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Calories Burned",
                    value = numberFormat.format(state.caloriesBurned),
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            if (state.totalWorkouts == 0) {
                Spacer(modifier = Modifier.height(16.dp))
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .background(GymFlowTheme.colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = GymFlowTheme.colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No workouts logged yet. Complete today's workout to see your real volume and personal records!",
                        style = GymFlowTheme.typography.BodyMedium,
                        color = GymFlowTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Progression Curve",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                    color = GymFlowTheme.colors.textPrimary
                )

                GymFlowBadge(
                    text = "${state.selectedTab.label} TREND",
                    backgroundColor = GymFlowTheme.colors.surfaceVariant,
                    textColor = GymFlowTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Bézier Weight Curve Chart
            ProgressWeightChart(
                points = state.weightProgressPoints,
                minY = state.minY,
                maxY = state.maxY,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Records Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Records (PRs)",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
                Text(
                    text = "ESTIMATED 1RM",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GymFlowTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.personalRecords.isEmpty()) {
                GymFlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = 16.dp
                ) {
                    Text(
                        text = "No personal records yet. Log your exercise sets during a workout to track your 1RM milestones.",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textTertiary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GymFlowTheme.colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = GymFlowTheme.colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = pr.exerciseName,
                                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Best: ${pr.bestSet} · ${pr.achievedDate}",
                                        style = GymFlowTheme.typography.Caption,
                                        color = GymFlowTheme.colors.textSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${pr.oneRepMaxKg.toInt()} kg",
                                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GymFlowTheme.colors.textPrimary
                                )
                                Text(
                                    text = "1RM Peak",
                                    style = GymFlowTheme.typography.Caption,
                                    color = GymFlowTheme.colors.textTertiary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
