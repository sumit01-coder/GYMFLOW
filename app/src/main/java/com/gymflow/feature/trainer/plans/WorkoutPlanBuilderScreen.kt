package com.gymflow.feature.trainer.plans

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
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

@Composable
fun WorkoutPlanBuilderScreen(
    initialMemberId: String,
    onBackClick: () -> Unit,
    onConfigureDayExercises: (String, String) -> Unit,
    onOpenExerciseLibrary: () -> Unit,
    onPlanSaved: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val daysOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    LaunchedEffect(initialMemberId) {
        viewModel.selectMember(initialMemberId)
    }

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Workout Plan Builder",
                    subtitle = "Studio Routine Designer",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.PLANS,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> onNavigateToDashboard()
                            TrainerBottomNavTab.MEMBERS -> onNavigateToMembers()
                            TrainerBottomNavTab.PLANS -> {}
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
                // Member Selector Card
                Text(
                    text = "Assign Athlete",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrainerDarkTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val selectedMember = uiState.memberOptions.firstOrNull { it.id == uiState.selectedMemberId }
                    ?: uiState.memberOptions.first()

                Box(modifier = Modifier.fillMaxWidth()) {
                    TrainerCard(
                        backgroundColor = TrainerDarkCard,
                        borderColor = TrainerDarkBorder,
                        contentPadding = 14.dp,
                        onClick = { memberDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF27272A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TrainerDarkTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedMember.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrainerDarkTextPrimary
                                    )
                                    Text(
                                        text = selectedMember.currentGoal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                            }
                            Text(
                                text = "Switch",
                                fontSize = 12.sp,
                                color = TrainerDarkTextSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = memberDropdownExpanded,
                        onDismissRequest = { memberDropdownExpanded = false },
                        modifier = Modifier
                            .background(TrainerDarkCard)
                            .border(1.dp, TrainerDarkBorder)
                    ) {
                        uiState.memberOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(opt.name, color = TrainerDarkTextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(opt.currentGoal, color = TrainerDarkTextSecondary, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    viewModel.selectMember(opt.id)
                                    memberDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Plan Title Input
                TrainerTextField(
                    value = uiState.planName,
                    onValueChange = { viewModel.setPlanName(it) },
                    label = "Program Name",
                    placeholder = "e.g. Hypertrophy Push-Pull-Legs 2026"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 7-Day Routine Schedule Builder Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7-Day Routine Split",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrainerDarkTextPrimary
                    )
                    Text(
                        text = "Tap to configure exercises",
                        fontSize = 12.sp,
                        color = TrainerDarkTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day cards list
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    daysOrder.forEach { dayName ->
                        val day = uiState.days[dayName]
                        val exerciseCount = day?.exercises?.size ?: 0
                        val isRest = day?.isRestDay == true

                        TrainerCard(
                            backgroundColor = if (isRest) Color(0xFF141416) else TrainerDarkCard,
                            borderColor = if (dayName == uiState.selectedDayOfWeek) Color(0xFF4ADE80).copy(alpha = 0.6f) else TrainerDarkBorder,
                            contentPadding = 14.dp,
                            onClick = {
                                viewModel.selectDay(dayName)
                                onConfigureDayExercises(dayName, uiState.planId)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isRest) Color(0xFF27272A)
                                                else Color(0xFF141416)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayName.take(3).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRest) TrainerDarkTextTertiary else Color(0xFF4ADE80)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = day?.title ?: dayName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TrainerDarkTextPrimary
                                        )
                                        Text(
                                            text = if (isRest) "Active Rest & Recovery" else "$exerciseCount exercises assigned",
                                            fontSize = 12.sp,
                                            color = if (isRest) TrainerDarkTextTertiary else TrainerDarkTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Day",
                                        tint = TrainerDarkTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = TrainerDarkTextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Save & Publish Plan CTA
                TrainerButton(
                    text = "Save & Publish Workout Plan",
                    onClick = {
                        viewModel.saveAndPublishPlan()
                        onPlanSaved()
                    },
                    style = TrainerButtonStyle.PRIMARY,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                if (uiState.showSuccessBanner) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TrainerCard(
                        backgroundColor = Color(0x264ADE80),
                        borderColor = Color(0xFF4ADE80),
                        contentPadding = 12.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plan successfully synchronized to athlete device!",
                                fontSize = 13.sp,
                                color = Color(0xFF4ADE80)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
