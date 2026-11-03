package com.gymflow.feature.member.activity

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowTheme

@Composable
fun MemberActivityScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHydration: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStopwatch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            GymFlowBottomBar(
                currentRoute = "member/activity",
                onNavigate = { route ->
                    when (route) {
                        "member/home" -> onNavigateToHome()
                        "member/schedule" -> onNavigateToSchedule()
                        "member/progress" -> onNavigateToProgress()
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
                text = "Activity & Habits",
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 26.sp),
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Hub Section
            Text(
                text = "DAILY HEALTH HABITS",
                style = GymFlowTheme.typography.Caption.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Hydration Tracker Shortcut Card
            ActivityHubCard(
                title = "Hydration Tracker",
                subtitle = "1,750 / 2,500 ml logged today (70%)",
                badgeText = "LOG WATER",
                icon = Icons.Default.WaterDrop,
                onClick = onNavigateToHydration
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Achievements & Badges Shortcut Card
            ActivityHubCard(
                title = "Achievements & Streaks",
                subtitle = "5-Day Workout Streak · 4 Milestones",
                badgeText = "VIEW ALL",
                icon = Icons.Default.EmojiEvents,
                onClick = onNavigateToAchievements
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Stopwatch & Split Timer Shortcut Card
            ActivityHubCard(
                title = "Athletic Stopwatch",
                subtitle = "Precision split timer & gym interval presets",
                badgeText = "TIMER",
                icon = Icons.Default.Timer,
                onClick = onNavigateToStopwatch
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Sessions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT HISTORY",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GymFlowTheme.colors.textSecondary
                )
                Text(
                    text = "3 sessions this week",
                    style = GymFlowTheme.typography.Caption,
                    color = GymFlowTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                SessionHistoryItem("Chest & Triceps Focus", "4 sets · 3,420 kg volume · 48 min", "Today, 08:30 AM", "320 kcal"),
                SessionHistoryItem("Leg Day Hypertrophy", "5 sets · 6,800 kg volume · 55 min", "Yesterday, 06:15 PM", "450 kcal"),
                SessionHistoryItem("Back & Biceps Pull", "4 sets · 4,100 kg volume · 42 min", "Nov 01, 07:00 AM", "290 kcal")
            ).forEach { item ->
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 14.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GymFlowTheme.colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = GymFlowTheme.colors.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = item.time,
                                        style = GymFlowTheme.typography.Caption,
                                        color = GymFlowTheme.colors.textTertiary
                                    )
                                }
                            }

                            GymFlowBadge(
                                text = item.calories,
                                backgroundColor = GymFlowTheme.colors.surfaceVariant,
                                textColor = GymFlowTheme.colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.details,
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class SessionHistoryItem(
    val title: String,
    val details: String,
    val time: String,
    val calories: String
)

@Composable
private fun ActivityHubCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymFlowCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                GymFlowBadge(
                    text = badgeText,
                    backgroundColor = GymFlowTheme.colors.surfaceVariant,
                    textColor = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = GymFlowTheme.colors.textTertiary
                )
            }
        }
    }
}
