package com.gymflow.feature.member.achievements

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowPrimaryButton
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

@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AchievementsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val unlockedCount = state.achievements.count { it.achievement.isUnlocked }
    val totalCount = state.achievements.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Achievements",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Streak Hero Banner
            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF000000),
                contentPadding = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${state.currentStreakDays}-Day Streak",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Longest: ${state.longestStreakDays} days · Discipline is destiny",
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF27272A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Weekly consistency dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val completed = listOf(true, true, true, true, true, false, false)
                    days.forEachIndexed { i, day ->
                        val isDone = completed[i]
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) Color.White else Color(0xFF27272A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                color = if (isDone) Color.White else Color(0xFF71717A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Counter Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Badges & Milestones",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MemberTextPrimary
                )
                Text(
                    text = "$unlockedCount / $totalCount Unlocked",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MemberTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Achievement List
            state.achievements.forEach { item ->
                AchievementCardItem(
                    item = item,
                    onClick = { viewModel.selectAchievement(item) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    state.selectedAchievement?.let { selected ->
        AchievementDetailDialog(
            item = selected,
            onDismiss = { viewModel.selectAchievement(null) }
        )
    }
}

@Composable
private fun AchievementCardItem(
    item: AchievementUiItem,
    onClick: () -> Unit
) {
    val isUnlocked = item.achievement.isUnlocked
    val icon = resolveAchievementIcon(item.achievement.iconName)

    GymFlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFF000000) else MemberSurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color.White else MemberTextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.achievement.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MemberTextPrimary
                    )

                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UNLOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    } else {
                        Text(
                            text = "${item.progressCurrent}/${item.progressTarget}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MemberTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.achievement.description,
                    fontSize = 12.sp,
                    color = MemberTextSecondary,
                    lineHeight = 16.sp
                )

                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { item.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(9999.dp)),
                        color = MemberBtnPrimaryBg,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementDetailDialog(
    item: AchievementUiItem,
    onDismiss: () -> Unit
) {
    val isUnlocked = item.achievement.isUnlocked
    val icon = resolveAchievementIcon(item.achievement.iconName)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) Color.Black else MemberSurfaceSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) Color.White else MemberTextTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.achievement.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MemberTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.achievement.description,
                    fontSize = 14.sp,
                    color = MemberTextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isUnlocked && item.achievement.unlockedAtEpochMs != null) {
                    val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        .format(Date(item.achievement.unlockedAtEpochMs))
                    Text(
                        text = "🏆 Unlocked on $dateStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF15803D)
                    )
                } else {
                    Text(
                        text = "Progress: ${item.progressCurrent} / ${item.progressTarget} (${(item.progressPercent * 100).toInt()}%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MemberTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            GymFlowPrimaryButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

private fun resolveAchievementIcon(iconName: String): ImageVector {
    return when (iconName) {
        "fitness_center" -> Icons.Default.FitnessCenter
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "calendar_month" -> Icons.Default.CalendarMonth
        "water_drop" -> Icons.Default.WaterDrop
        "military_tech" -> Icons.Default.MilitaryTech
        "scale" -> Icons.Default.Scale
        else -> Icons.Default.EmojiEvents
    }
}
