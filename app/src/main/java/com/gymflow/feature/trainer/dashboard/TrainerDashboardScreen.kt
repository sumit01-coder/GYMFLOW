package com.gymflow.feature.trainer.dashboard

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TrendingUp
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
import com.gymflow.feature.trainer.components.TrainerBadge
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerKpiCard
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun TrainerDashboardScreen(
    onCreatePlanClick: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToMemberProfile: (String) -> Unit,
    onNavigateToPlanBuilder: () -> Unit,
    onNavigateToExerciseLibrary: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToJoinCode: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainerTheme {
        Scaffold(
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.DASHBOARD,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> {}
                            TrainerBottomNavTab.MEMBERS -> onNavigateToMembers()
                            TrainerBottomNavTab.PLANS -> onNavigateToPlanBuilder()
                            TrainerBottomNavTab.ANALYTICS -> onNavigateToAnalytics()
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
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4ADE80))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRAINER STUDIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80),
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = uiState.greeting,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Invitation Code Shortcut Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TrainerDarkCard)
                                .border(1.dp, TrainerDarkBorder, CircleShape)
                                .clickable(onClick = onNavigateToJoinCode),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Trainer Join Code",
                                tint = TrainerDarkTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Coach Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TrainerDarkCard)
                                .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.5f), CircleShape)
                                .clickable(onClick = onNavigateToSettings),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Settings",
                                tint = TrainerDarkTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4-KPI Grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrainerKpiCard(
                        title = "TOTAL MEMBERS",
                        value = "${uiState.kpis.totalMembers}",
                        subtitle = "Connected in Studio",
                        icon = Icons.Default.Person,
                        accentColor = TrainerDarkTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TrainerKpiCard(
                        title = "ACTIVE MEMBERS",
                        value = "${uiState.kpis.activeMembers}",
                        subtitle = "Training this week",
                        icon = Icons.Default.CheckCircle,
                        accentColor = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TrainerKpiCard(
                        title = "COMPLETED TODAY",
                        value = "${uiState.kpis.completedToday}",
                        subtitle = "Sessions finished",
                        icon = Icons.Default.AssignmentTurnedIn,
                        accentColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                    TrainerKpiCard(
                        title = "PENDING REVIEWS",
                        value = "${uiState.kpis.pendingReviews}",
                        subtitle = "Logs awaiting review",
                        icon = Icons.Default.PendingActions,
                        accentColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary CTA: + Create Workout Plan
                TrainerCard(
                    backgroundColor = TrainerDarkCard,
                    borderColor = Color(0xFF4ADE80).copy(alpha = 0.4f),
                    contentPadding = 18.dp,
                    onClick = onCreatePlanClick
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Build Athlete Routine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrainerDarkTextPrimary
                            )
                            Text(
                                text = "Configure 7-day splits, supersets and exercise rest intervals",
                                fontSize = 12.sp,
                                color = TrainerDarkTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Plan",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Connected Members Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connected Members",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrainerDarkTextPrimary
                    )
                    Text(
                        text = "View All (${uiState.kpis.totalMembers})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4ADE80),
                        modifier = Modifier.clickable { onNavigateToMembers() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connected Members List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.recentMembers.forEach { member ->
                        TrainerCard(
                            backgroundColor = TrainerDarkCard,
                            borderColor = TrainerDarkBorder,
                            contentPadding = 14.dp,
                            onClick = { onNavigateToMemberProfile(member.memberId) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Initial Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF27272A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.name.take(2).uppercase(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TrainerDarkTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = member.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TrainerDarkTextPrimary
                                        )
                                        Text(
                                            text = member.currentWorkout,
                                            fontSize = 12.sp,
                                            color = TrainerDarkTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TrainerBadge(
                                        text = member.status,
                                        status = member.status
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open",
                                        tint = TrainerDarkTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Studio Tools Grid
                Text(
                    text = "Studio Quick Tools",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = TrainerDarkCard,
                        contentPadding = 12.dp,
                        onClick = onNavigateToExerciseLibrary
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Library", fontSize = 12.sp, color = TrainerDarkTextPrimary)
                        }
                    }

                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = TrainerDarkCard,
                        contentPadding = 12.dp,
                        onClick = onNavigateToAnalytics
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Analytics", fontSize = 12.sp, color = TrainerDarkTextPrimary)
                        }
                    }

                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = TrainerDarkCard,
                        contentPadding = 12.dp,
                        onClick = onNavigateToJoinCode
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Join Code", fontSize = 12.sp, color = TrainerDarkTextPrimary)
                        }
                    }
                }
            }
        }
    }
}
