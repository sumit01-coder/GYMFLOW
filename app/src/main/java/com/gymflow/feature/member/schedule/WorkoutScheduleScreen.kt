package com.gymflow.feature.member.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WorkoutScheduleScreen(
    viewModel: WorkoutScheduleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    WorkoutScheduleContent(
        uiState = uiState,
        onSelectDay = { dayItem -> viewModel.selectDate(dayItem.date) },
        onOpenCalendar = { viewModel.setDatePickerOpen(true) },
        onDismissCalendar = { viewModel.setDatePickerOpen(false) },
        onDatePicked = { date -> viewModel.selectDate(date) },
        onJumpToToday = { viewModel.jumpToToday() },
        onNavigateBack = onNavigateBack,
        onNavigateToHome = onNavigateToHome,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToActivity = onNavigateToActivity,
        onNavigateToProfile = onNavigateToProfile,
        onSelectWorkout = onSelectWorkout,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScheduleContent(
    uiState: WorkoutScheduleUiState,
    onSelectDay: (ScheduleDayItem) -> Unit,
    onOpenCalendar: () -> Unit,
    onDismissCalendar: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
    onJumpToToday: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            GymFlowBottomBar(
                currentRoute = "member/schedule",
                onNavigate = { route ->
                    when (route) {
                        "member/home" -> onNavigateToHome()
                        "member/progress" -> onNavigateToProgress()
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
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GymFlowTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Workout Schedule",
                        style = GymFlowTheme.typography.Heading1.copy(fontSize = 22.sp),
                        color = GymFlowTheme.colors.textPrimary
                    )
                }

                // Interactive Calendar Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .clickable(onClick = onOpenCalendar),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Open Calendar",
                        modifier = Modifier.size(20.dp),
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7-Day Horizontal Strip Header (Current Month & Year + Today indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.currentMonthYearLabel,
                        style = GymFlowTheme.typography.Caption.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    if (uiState.isLookingAtToday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GymFlowTheme.colors.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = GymFlowTheme.typography.Caption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }
                    }
                }

                if (!uiState.isLookingAtToday) {
                    TextButton(
                        onClick = onJumpToToday,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Jump to Today",
                            style = GymFlowTheme.typography.Caption.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7-Day Horizontal Strip
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.weekDays) { day ->
                    val isSelected = day.isSelected
                    val pillBg = if (isSelected) GymFlowBlack else GymFlowTheme.colors.surfaceVariant
                    val textCol = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textPrimary
                    val subTextCol = if (isSelected) GymFlowWhite.copy(alpha = 0.8f) else GymFlowTheme.colors.textSecondary
                    val borderModifier = if (day.isToday && !isSelected) {
                        Modifier.border(1.5.dp, GymFlowBlack, GymFlowShapesTokens.CardMain)
                    } else {
                        Modifier
                    }

                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .then(borderModifier)
                            .clip(GymFlowShapesTokens.CardMain)
                            .background(pillBg)
                            .clickable { onSelectDay(day) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayOfWeek,
                            style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                            color = subTextCol
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.dayNumber.toString(),
                            style = GymFlowTheme.typography.BodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = textCol
                        )
                        if (day.isCompleted) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(GymFlowGreen, shape = CircleShape)
                            )
                        } else if (day.isToday && isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(GymFlowWhite, shape = CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Workout List Header
            val selectedDayFull = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val selectedDayShort = uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Routine",
                    style = GymFlowTheme.typography.Heading2,
                    color = GymFlowTheme.colors.textPrimary
                )
                Text(
                    text = "$selectedDayFull",
                    style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = GymFlowTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            uiState.scheduleEntries.forEach { entry ->
                val isCurrentSelectedDay = entry.dayName.equals(selectedDayShort, ignoreCase = true)
                ScheduleEntryCard(
                    entry = entry,
                    isHighlighted = isCurrentSelectedDay,
                    onClick = {
                        if (!entry.isRestDay) {
                            onSelectWorkout(entry.title)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Material 3 Calendar Date Picker Dialog
    if (uiState.isDatePickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = onDismissCalendar,
            shape = RoundedCornerShape(24.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val pickedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onDatePicked(pickedDate)
                        }
                        onDismissCalendar()
                    }
                ) {
                    Text(
                        text = "Select",
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCalendar) {
                    Text(
                        text = "Cancel",
                        style = GymFlowTheme.typography.BodyMedium,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = GymFlowTheme.colors.surface,
                titleContentColor = GymFlowTheme.colors.textPrimary,
                headlineContentColor = GymFlowTheme.colors.textPrimary,
                weekdayContentColor = GymFlowTheme.colors.textSecondary,
                subheadContentColor = GymFlowTheme.colors.textPrimary,
                yearContentColor = GymFlowTheme.colors.textPrimary,
                currentYearContentColor = GymFlowTheme.colors.textPrimary,
                selectedYearContentColor = GymFlowWhite,
                selectedYearContainerColor = GymFlowBlack,
                dayContentColor = GymFlowTheme.colors.textPrimary,
                selectedDayContentColor = GymFlowWhite,
                selectedDayContainerColor = GymFlowBlack,
                todayContentColor = GymFlowBlack,
                todayDateBorderColor = GymFlowBlack
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = {
                    Text(
                        text = "SELECT DATE",
                        style = GymFlowTheme.typography.Caption.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp)
                    )
                },
                headline = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    val formattedHeadline = selectedMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault()))
                    } ?: "Choose Date"
                    Text(
                        text = formattedHeadline,
                        style = GymFlowTheme.typography.Heading2.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = GymFlowTheme.colors.textPrimary,
                        modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 12.dp, end = 24.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = GymFlowTheme.colors.surface,
                    titleContentColor = GymFlowTheme.colors.textPrimary,
                    headlineContentColor = GymFlowTheme.colors.textPrimary,
                    weekdayContentColor = GymFlowTheme.colors.textSecondary,
                    subheadContentColor = GymFlowTheme.colors.textPrimary,
                    yearContentColor = GymFlowTheme.colors.textPrimary,
                    currentYearContentColor = GymFlowTheme.colors.textPrimary,
                    selectedYearContentColor = GymFlowWhite,
                    selectedYearContainerColor = GymFlowBlack,
                    dayContentColor = GymFlowTheme.colors.textPrimary,
                    selectedDayContentColor = GymFlowWhite,
                    selectedDayContainerColor = GymFlowBlack,
                    todayContentColor = GymFlowBlack,
                    todayDateBorderColor = GymFlowBlack
                )
            )
        }
    }
}

@Composable
private fun ScheduleEntryCard(
    entry: ScheduleWorkoutEntry,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 14.dp,
        borderColor = if (isHighlighted) GymFlowBlack else GymFlowBorder,
        borderWidth = if (isHighlighted) 1.5.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Circle: Green check circle (completed) or empty circle (upcoming)
                if (entry.isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GymFlowGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = GymFlowWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (entry.isRestDay) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GymFlowTheme.colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = "Rest",
                            tint = GymFlowTheme.colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(2.dp, GymFlowBorder, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.dayName,
                            style = GymFlowTheme.typography.Caption.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isHighlighted) GymFlowBlack else GymFlowTheme.colors.textSecondary
                        )
                        Text(
                            text = " · ${entry.title}",
                            style = GymFlowTheme.typography.BodyMedium.copy(
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = GymFlowTheme.colors.textPrimary
                        )
                        if (isHighlighted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GymFlowBlack)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SELECTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite
                                )
                            }
                        }
                    }
                    if (!entry.isRestDay) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${entry.exerciseCount} exercises · ${entry.estimatedMinutes} min",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Recovery & Hydration focus",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                    }
                }
            }

            if (!entry.isRestDay) {
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
