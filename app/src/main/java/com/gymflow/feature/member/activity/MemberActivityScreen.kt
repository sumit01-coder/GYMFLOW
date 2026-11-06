package com.gymflow.feature.member.activity

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@Composable
fun MemberActivityScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHydration: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToStopwatch: () -> Unit = {},
    viewModel: MemberActivityViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var sessionToDelete by remember { mutableStateOf<ActivityWorkoutItem?>(null) }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Activity & Habits",
                        style = GymFlowTheme.typography.Heading1.copy(fontSize = 24.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-time health, streak & workout log",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GymFlowGreen.copy(alpha = 0.12f))
                        .border(1.dp, GymFlowGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE SYNC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Hub Section
            Text(
                text = "DAILY HEALTH HABITS",
                style = GymFlowTheme.typography.Caption.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Hydration Tracker Shortcut Card (REAL DATA)
            ActivityHubCard(
                title = "Hydration Tracker",
                subtitle = state.hydrationSubtitle,
                badgeText = "LOG WATER",
                icon = Icons.Default.WaterDrop,
                iconTint = Color(0xFF0284C7),
                iconBg = Color(0xFFE0F2FE),
                onClick = onNavigateToHydration
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Achievements & Badges Shortcut Card (REAL DATA)
            ActivityHubCard(
                title = "Achievements & Streaks",
                subtitle = state.achievementsSubtitle,
                badgeText = "VIEW ALL",
                icon = Icons.Default.EmojiEvents,
                iconTint = Color(0xFFD97706),
                iconBg = Color(0xFFFEF3C7),
                onClick = onNavigateToAchievements
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Stopwatch & Split Timer Shortcut Card
            ActivityHubCard(
                title = "Athletic Stopwatch & Timer",
                subtitle = "Precision countdown & interval reverse timer",
                badgeText = "TIMER",
                icon = Icons.Default.Timer,
                iconTint = Color(0xFF4F46E5),
                iconBg = Color(0xFFEEF2FF),
                onClick = onNavigateToStopwatch
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Sessions Section (100% REAL DATABASE DATA)
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
                    text = state.sessionsThisWeekLabel,
                    style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.Medium),
                    color = GymFlowTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date Filter Bar
            WorkoutHistoryFilterBar(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.setHistoryFilter(it) },
                onCustomRange = { start, end -> viewModel.setCustomRange(start, end) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.workoutHistory.isEmpty()) {
                // Empty state card when user has no completed sessions yet
                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(GymFlowTheme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = GymFlowTheme.colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No Workouts Logged Yet",
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Complete your scheduled workout to start building your real training history, volume load, and calorie analytics!",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymFlowBlack)
                                .clickable(onClick = onNavigateToSchedule)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = GymFlowWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Today's Workout",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite
                                )
                            }
                        }
                    }
                }
            } else {
                // Display REAL completed sessions
                state.workoutHistory.forEach { item ->
                    GymFlowCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        contentPadding = 14.dp,
                        onLongClick = {
                            sessionToDelete = item
                        }
                    ) {
                        Column {
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
                                            .size(36.dp)
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GymFlowTheme.colors.textPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.timeFormatted,
                                            style = GymFlowTheme.typography.Caption,
                                            color = GymFlowTheme.colors.textTertiary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GymFlowTheme.colors.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = item.caloriesFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.details,
                                style = GymFlowTheme.typography.Caption.copy(fontSize = 12.sp),
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (sessionToDelete != null) {
            AlertDialog(
                onDismissRequest = { sessionToDelete = null },
                title = {
                    Text(
                        text = "Delete Workout Session?",
                        style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove \"${sessionToDelete?.title}\" (${sessionToDelete?.timeFormatted}) from your workout history? This cannot be undone.",
                        style = GymFlowTheme.typography.BodyMedium,
                        color = GymFlowTheme.colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val toDelete = sessionToDelete
                            sessionToDelete = null
                            if (toDelete != null) {
                                viewModel.deleteSession(toDelete.id)
                            }
                        }
                    ) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { sessionToDelete = null }) {
                        Text(
                            text = "Cancel",
                            color = GymFlowTheme.colors.textSecondary
                        )
                    }
                },
                containerColor = GymFlowTheme.colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun ActivityHubCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    iconTint: Color = GymFlowBlack,
    iconBg: Color = GymFlowTheme.colors.surfaceVariant,
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutHistoryFilterBar(
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    onCustomRange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HistoryFilter.values().toList()) { filter ->
            val isSelected = selectedFilter == filter
            val isCustom = filter == HistoryFilter.CUSTOM

            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isCustom) {
                        showDatePicker = true
                    } else {
                        onFilterSelected(filter)
                    }
                },
                label = {
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GymFlowBlack,
                    selectedLabelColor = GymFlowWhite,
                    containerColor = GymFlowTheme.colors.surfaceVariant,
                    labelColor = GymFlowTheme.colors.textPrimary
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = datePickerState.selectedStartDateMillis
                    val end = datePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onCustomRange(start, end + 86_400_000L - 1)
                    }
                    showDatePicker = false
                }) {
                    Text("Apply", color = GymFlowBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
