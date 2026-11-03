package com.gymflow.feature.member.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.MetricCard
import com.gymflow.core.theme.GymFlowTheme

@Composable
fun MemberProgressScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRangeIndex by remember { mutableIntStateOf(0) }
    val ranges = listOf("7D", "30D", "3M", "6M", "1Y")

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
                text = "Progress",
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 26.sp),
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Range Selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ranges.forEachIndexed { index, range ->
                    SegmentedButton(
                        selected = selectedRangeIndex == index,
                        onClick = { selectedRangeIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ranges.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = GymFlowTheme.colors.primaryButtonBg,
                            activeContentColor = GymFlowTheme.colors.primaryButtonFg,
                            inactiveContainerColor = GymFlowTheme.colors.surfaceVariant,
                            inactiveContentColor = GymFlowTheme.colors.textSecondary
                        )
                    ) {
                        Text(text = range, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    value = "18",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Total Sets",
                    value = "342",
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
                    value = "4,126",
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Calories Burned",
                    value = "5,480",
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Weight Trend",
                style = GymFlowTheme.typography.Heading2,
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            GymFlowCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Text(
                                text = "67.8 kg",
                                style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }

                        Column {
                            Text(
                                text = "Change (7 Days)",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                            Text(
                                text = "-0.4 kg",
                                style = GymFlowTheme.typography.Heading2.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowTheme.colors.accentActive
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
