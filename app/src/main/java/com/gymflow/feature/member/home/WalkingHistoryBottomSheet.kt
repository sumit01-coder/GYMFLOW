package com.gymflow.feature.member.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.database.DailyStepsEntity
import com.gymflow.core.database.OutdoorWorkoutEntity
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowRed
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkingHistoryBottomSheet(
    uiState: MemberHomeUiState,
    onDismiss: () -> Unit,
    onUpdateStepGoal: (Int) -> Unit,
    onDeleteOutdoorWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    var workoutPendingDelete by remember { mutableStateOf<OutdoorWorkoutEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GymFlowTheme.colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GymFlowTheme.colors.textSecondary.copy(alpha = 0.3f))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GymFlowGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = GymFlowGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Walking & Steps Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowTheme.colors.textPrimary
                        )
                        Text(
                            text = "Mobility history & outdoor GPS records",
                            fontSize = 12.sp,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GymFlowTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly Highlights Banner
            WeeklyPerformanceCard(
                avgSteps = uiState.weeklyAverageSteps,
                totalDistanceKm = uiState.weeklyTotalDistanceKm,
                goalsHit = uiState.daysGoalReachedCount,
                streak = uiState.currentStreakDays
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Target Quick Selector
            StepGoalSelectorSection(
                currentGoal = uiState.stepGoal,
                onSelectGoal = onUpdateStepGoal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented Tabs: Daily Steps Log vs Outdoor GPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(4.dp)
            ) {
                val stepLogCount = uiState.recentStepsHistory.size
                val outdoorCount = uiState.recentOutdoorWorkouts.size

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) GymFlowTheme.colors.surface else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Steps ($stepLogCount)",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) GymFlowTheme.colors.surface else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Outdoor GPS ($outdoorCount)",
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            if (selectedTab == 0) {
                DailyStepsListTab(
                    stepsList = uiState.recentStepsHistory,
                    defaultGoal = uiState.stepGoal
                )
            } else {
                OutdoorWorkoutsListTab(
                    workouts = uiState.recentOutdoorWorkouts,
                    onDeleteRequest = { workout -> workoutPendingDelete = workout }
                )
            }
        }
    }

    // Deletion confirmation dialog for outdoor workout
    if (workoutPendingDelete != null) {
        val workout = workoutPendingDelete!!
        AlertDialog(
            onDismissRequest = { workoutPendingDelete = null },
            title = {
                Text(
                    text = "Delete Outdoor ${workout.workoutType.lowercase().replaceFirstChar { it.uppercase() }}?",
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this recorded session (%.2f km, %d steps)? This cannot be undone.".format(
                        workout.totalDistanceMeters / 1000f,
                        workout.totalSteps
                    ),
                    color = GymFlowTheme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteOutdoorWorkout(workout.id)
                        workoutPendingDelete = null
                    }
                ) {
                    Text("Delete", color = GymFlowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutPendingDelete = null }) {
                    Text("Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            },
            containerColor = GymFlowTheme.colors.surface
        )
    }
}

@Composable
private fun WeeklyPerformanceCard(
    avgSteps: Int,
    totalDistanceKm: Float,
    goalsHit: Int,
    streak: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GymFlowBlack)
            .border(1.dp, GymFlowGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-DAY MOBILITY SNAPSHOT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GymFlowGreen,
                    letterSpacing = 1.sp
                )
                if (streak > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GymFlowGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🔥 $streak-Day Streak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(
                    label = "DAILY AVG",
                    value = "%,d".format(avgSteps),
                    unit = "steps"
                )
                StatPill(
                    label = "TOTAL DISTANCE",
                    value = "%.1f".format(totalDistanceKm),
                    unit = "km"
                )
                StatPill(
                    label = "GOALS HIT",
                    value = "$goalsHit / 7",
                    unit = "days"
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    unit: String
) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = GymFlowWhite.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = GymFlowWhite
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 11.sp,
                color = GymFlowGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun StepGoalSelectorSection(
    currentGoal: Int,
    onSelectGoal: (Int) -> Unit
) {
    val presetGoals = listOf(6000, 8000, 10000, 12000, 15000)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Step Target",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GymFlowTheme.colors.textPrimary
            )
            Text(
                text = "Goal: %,d steps".format(currentGoal),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GymFlowGreen
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetGoals.forEach { goal ->
                val isSelected = goal == currentGoal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) GymFlowGreen else GymFlowTheme.colors.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GymFlowGreen else GymFlowBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectGoal(goal) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${goal / 1000}k",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GymFlowBlack else GymFlowTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyStepsListTab(
    stepsList: List<DailyStepsEntity>,
    defaultGoal: Int
) {
    if (stepsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = GymFlowTheme.colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Daily Steps Recorded Yet",
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep your phone with you as you walk to automatically record daily progress.",
                    fontSize = 12.sp,
                    color = GymFlowTheme.colors.textSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stepsList, key = { it.id }) { item ->
                DailyStepItemCard(item = item, defaultGoal = defaultGoal)
            }
        }
    }
}

@Composable
private fun DailyStepItemCard(
    item: DailyStepsEntity,
    defaultGoal: Int
) {
    val target = if (item.targetGoal > 0) item.targetGoal else defaultGoal
    val progress = (item.stepCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val isGoalMet = item.stepCount >= target && item.stepCount > 0

    val formattedDate = formatHumanDate(item.date)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowTheme.colors.textPrimary
                    )
                }

                if (isGoalMet) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GymFlowGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, GymFlowGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GymFlowGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Goal Reached",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GymFlowGreen
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Numbers
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(item.stepCount),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ %,d steps".format(target),
                    fontSize = 12.sp,
                    color = GymFlowTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress track
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isGoalMet) GymFlowGreen else GymFlowGreen.copy(alpha = 0.7f),
                trackColor = GymFlowTheme.colors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics: Distance, Calories, Active Minutes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.2f km".format(item.distanceMeters / 1000f),
                        fontSize = 12.sp,
                        color = GymFlowTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.caloriesKcal} kcal",
                        fontSize = 12.sp,
                        color = GymFlowTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                val estActiveMins = if (item.activeMinutes > 0) {
                    item.activeMinutes
                } else {
                    (item.stepCount / 100).coerceAtLeast(1)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$estActiveMins min",
                        fontSize = 12.sp,
                        color = GymFlowTheme.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OutdoorWorkoutsListTab(
    workouts: List<OutdoorWorkoutEntity>,
    onDeleteRequest: (OutdoorWorkoutEntity) -> Unit
) {
    if (workouts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = GymFlowTheme.colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Outdoor GPS Sessions Yet",
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start an 'Outdoor Run' or 'Outdoor Walk' to record GPS routes, pace and distance.",
                    fontSize = 12.sp,
                    color = GymFlowTheme.colors.textSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(workouts, key = { it.id }) { workout ->
                OutdoorWorkoutItemCard(
                    workout = workout,
                    onDelete = { onDeleteRequest(workout) }
                )
            }
        }
    }
}

@Composable
private fun OutdoorWorkoutItemCard(
    workout: OutdoorWorkoutEntity,
    onDelete: () -> Unit
) {
    val isRun = workout.workoutType.equals("RUN", ignoreCase = true)
    val mins = workout.durationSeconds / 60
    val secs = workout.durationSeconds % 60
    val formattedDuration = "%02d:%02d".format(mins, secs)
    val distKm = workout.totalDistanceMeters / 1000f

    val paceStr = if (workout.avgPaceSecondsPerKm > 0f) {
        val paceM = (workout.avgPaceSecondsPerKm / 60).toInt()
        val paceS = (workout.avgPaceSecondsPerKm % 60).toInt()
        "%d'%02d\"/km".format(paceM, paceS)
    } else {
        "--:--"
    }

    val formattedDate = formatEpochDate(workout.startTimeEpochMs)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRun) GymFlowGreen.copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRun) Icons.Default.DirectionsRun else Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = if (isRun) GymFlowGreen else Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isRun) "Outdoor Run" else "Outdoor Walk",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GymFlowTheme.colors.textPrimary
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete session",
                        tint = GymFlowTheme.colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Distance
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "%.2f".format(distKm),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = GymFlowTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "km",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GymFlowGreen,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Grid: Duration, Pace, Calories, Steps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textSecondary)
                    Text(formattedDuration, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textPrimary)
                }
                Column {
                    Text("PACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textSecondary)
                    Text(paceStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textPrimary)
                }
                Column {
                    Text("CALORIES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textSecondary)
                    Text("${workout.caloriesBurned} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textPrimary)
                }
                Column {
                    Text("STEPS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textSecondary)
                    Text("%,d".format(workout.totalSteps), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GymFlowTheme.colors.textPrimary)
                }
            }
        }
    }
}

private fun formatHumanDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val today = LocalDate.now()
        when (date) {
            today -> "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
            today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatEpochDate(epochMs: Long): String {
    return try {
        val instant = Instant.ofEpochMilli(epochMs)
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = instant.atZone(zoneId)
        val today = LocalDate.now()
        val sessionDate = zonedDateTime.toLocalDate()
        val timeStr = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))

        when (sessionDate) {
            today -> "Today · $timeStr"
            today.minusDays(1) -> "Yesterday · $timeStr"
            else -> "${sessionDate.format(DateTimeFormatter.ofPattern("MMM d"))} · $timeStr"
        }
    } catch (e: Exception) {
        "Recent Session"
    }
}

