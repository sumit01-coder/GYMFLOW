package com.gymflow.feature.trainer.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerKpiCard
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun TrainerAnalyticsScreen(
    onBackClick: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerAnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Studio Analytics",
                    subtitle = "Performance & Volume Overview",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.ANALYTICS,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> onNavigateToDashboard()
                            TrainerBottomNavTab.MEMBERS -> onNavigateToMembers()
                            TrainerBottomNavTab.PLANS -> onNavigateToPlans()
                            TrainerBottomNavTab.ANALYTICS -> {}
                            TrainerBottomNavTab.SETTINGS -> onNavigateToSettings()
                        }
                    }
                )
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
                // Top Completion Rate Card with Circular Metric
                TrainerCard(contentPadding = 18.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STUDIO COMPLETION RATE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrainerDarkTextTertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.completionRatePercentage}%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4ADE80)
                            )
                            Text(
                                text = "${uiState.activeAthletesCount} of ${uiState.totalAthletesCount} athletes completed all scheduled sessions this week",
                                fontSize = 12.sp,
                                color = TrainerDarkTextSecondary,
                                modifier = Modifier.padding(top = 4.dp),
                                lineHeight = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF27272A),
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = { uiState.completionRatePercentage / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF4ADE80),
                                strokeWidth = 8.dp
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerKpiCard(
                        title = "TOTAL STUDIO VOLUME",
                        value = "48,200 kg",
                        subtitle = "+14% vs last month",
                        icon = Icons.Default.FitnessCenter,
                        accentColor = TrainerDarkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TrainerKpiCard(
                        title = "AVG SESSIONS / WK",
                        value = "${uiState.averageSessionsPerWeek}",
                        subtitle = "High athlete discipline",
                        icon = Icons.Default.Schedule,
                        accentColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Workout Distribution Bar Chart
                Text(
                    text = "Workout Muscle Distribution",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )
                Text(
                    text = "Weekly aggregate across all connected member routines",
                    fontSize = 12.sp,
                    color = TrainerDarkTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TrainerCard(contentPadding = 16.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.muscleDistributions.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.muscleGroup,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TrainerDarkTextPrimary
                                    )
                                    Text(
                                        text = "${item.percentage}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4ADE80)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Horizontal progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFF27272A))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(item.percentage / 100f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(Color(0xFF4ADE80))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Peak Training Times
                Text(
                    text = "Studio Peak Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrainerCard(contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.peakHours.forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = slot.timeSlot,
                                    fontSize = 13.sp,
                                    color = TrainerDarkTextSecondary
                                )
                                Text(
                                    text = "${slot.memberCount} active athletes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (slot.memberCount > 10) Color(0xFFFBBF24) else TrainerDarkTextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // PDF Export Report Card
                TrainerCard(
                    backgroundColor = Color(0xFF141416),
                    borderColor = Color(0xFF27272A),
                    contentPadding = 16.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x26EF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Monthly Studio PDF Report",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TrainerDarkTextPrimary
                                )
                                Text(
                                    text = "Includes volume curves, attendance and individual logs",
                                    fontSize = 12.sp,
                                    color = TrainerDarkTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TrainerButton(
                            text = if (uiState.isExportingPdf) "Compiling Studio PDF..." else "Export Studio PDF Report",
                            onClick = { viewModel.exportPdfReport() },
                            isLoading = uiState.isExportingPdf,
                            style = TrainerButtonStyle.PRIMARY,
                            height = 44.dp
                        )

                        if (uiState.pdfExportComplete) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x264ADE80))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Report generated: ${uiState.exportedPdfFileName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
