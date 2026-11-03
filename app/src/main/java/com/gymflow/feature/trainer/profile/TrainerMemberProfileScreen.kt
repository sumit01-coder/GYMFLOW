package com.gymflow.feature.trainer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NoteAlt
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
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerTextField
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainerMemberProfileScreen(
    memberId: String,
    onBackClick: () -> Unit,
    onViewFullProgress: () -> Unit,
    onEditWorkoutPlan: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerMemberProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val member = uiState.profile

    val notePresets = listOf(
        "Increase weight on bench press next week",
        "Excellent chest contraction and tempo today!",
        "Focus on controlled eccentrics on cable fly",
        "Remember to hydrate and hit protein goal"
    )

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = member.name,
                    subtitle = "Athlete Overview",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TrainerDarkBg)
                        .border(1.dp, TrainerDarkBorder)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerButton(
                        text = "View Progress",
                        onClick = onViewFullProgress,
                        style = TrainerButtonStyle.SECONDARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TrainerButton(
                        text = "Edit Workout Plan",
                        onClick = onEditWorkoutPlan,
                        style = TrainerButtonStyle.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
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
                // Member Identity Card
                TrainerCard(
                    backgroundColor = TrainerDarkCard,
                    borderColor = TrainerDarkBorder,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF27272A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(2).uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrainerDarkTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrainerDarkTextPrimary
                                )
                                Text(
                                    text = member.email,
                                    fontSize = 12.sp,
                                    color = TrainerDarkTextSecondary
                                )
                                Text(
                                    text = member.joinedDate,
                                    fontSize = 11.sp,
                                    color = TrainerDarkTextTertiary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        TrainerBadge(
                            text = member.status,
                            status = member.status
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            Text("GOAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(member.goal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4ADE80))
                        }
                    }

                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            Text("CURRENT WEIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${member.currentWeightKg} kg", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrainerDarkTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            Text("ATTENDANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${member.attendanceRate}% (${member.workoutsCompletedThisMonth} sessions)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFBBF24))
                        }
                    }

                    TrainerCard(
                        modifier = Modifier.weight(1f),
                        contentPadding = 12.dp
                    ) {
                        Column {
                            Text("PHYSIQUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TrainerDarkTextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${member.heightCm.toInt()} cm · ${member.age} yrs", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrainerDarkTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Active Routine Section
                Text(
                    text = "Active Routine Schedule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )
                Text(
                    text = member.routineTitle,
                    fontSize = 13.sp,
                    color = Color(0xFF4ADE80),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Weekly routine list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    member.weeklyDays.forEach { day ->
                        TrainerCard(
                            backgroundColor = if (day.isToday) Color(0xFF1E241E) else TrainerDarkCard,
                            borderColor = if (day.isToday) Color(0xFF4ADE80).copy(alpha = 0.5f) else TrainerDarkBorder,
                            contentPadding = 12.dp
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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (day.isCompleted) Color(0x264ADE80)
                                                else if (day.isRestDay) Color(0xFF27272A)
                                                else Color(0xFF141416)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.dayOfWeek,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (day.isCompleted) Color(0xFF4ADE80) else TrainerDarkTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = day.routineTitle,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TrainerDarkTextPrimary
                                        )
                                        if (day.isToday) {
                                            Text(
                                                text = "Scheduled for Today",
                                                fontSize = 11.sp,
                                                color = Color(0xFF4ADE80)
                                            )
                                        }
                                    }
                                }

                                if (day.isCompleted) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = Color(0xFF4ADE80),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Done", fontSize = 11.sp, color = Color(0xFF4ADE80))
                                    }
                                } else if (day.isRestDay) {
                                    Text("Rest Day", fontSize = 11.sp, color = TrainerDarkTextTertiary)
                                } else {
                                    Text("Upcoming", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weight History List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Weight Logs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrainerDarkTextPrimary
                    )
                    Text(
                        text = "Net: -2.5 kg total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4ADE80)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TrainerCard(contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        member.weightHistory.forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonitorWeight,
                                        contentDescription = null,
                                        tint = TrainerDarkTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = log.date,
                                        fontSize = 13.sp,
                                        color = TrainerDarkTextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${log.weightKg} kg",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrainerDarkTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${log.deltaKg} kg",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Trainer Feedback Note Composer
                Text(
                    text = "Coach Feedback & Notes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )
                Text(
                    text = "Notes are pushed instantly to Sarah's member mobile dashboard",
                    fontSize = 12.sp,
                    color = TrainerDarkTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrainerCard(contentPadding = 16.dp) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TrainerTextField(
                            value = uiState.feedbackInput,
                            onValueChange = { viewModel.onFeedbackInputChanged(it) },
                            placeholder = "Write feedback or routine guidance for Sarah...",
                            singleLine = false,
                            modifier = Modifier.height(96.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quick Presets:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrainerDarkTextTertiary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            notePresets.forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(Color(0xFF27272A))
                                        .clickable { viewModel.applyPresetNote(preset) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 11.sp,
                                        color = TrainerDarkTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TrainerButton(
                            text = "Send Note to Member",
                            onClick = { viewModel.sendFeedbackNote() },
                            style = TrainerButtonStyle.PRIMARY,
                            height = 42.dp,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        if (uiState.isNoteSentNoticeVisible) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x264ADE80))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Feedback successfully pushed to Sarah's device",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Notes History Timeline
                Text(
                    text = "Previous Feedback Logs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    member.notesHistory.forEach { note ->
                        TrainerCard(
                            backgroundColor = Color(0xFF141416),
                            borderColor = TrainerDarkBorder,
                            contentPadding = 12.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = note.author,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4ADE80)
                                    )
                                    Text(
                                        text = note.timestamp,
                                        fontSize = 11.sp,
                                        color = TrainerDarkTextTertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.text,
                                    fontSize = 13.sp,
                                    color = TrainerDarkTextSecondary,
                                    lineHeight = 18.sp
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
