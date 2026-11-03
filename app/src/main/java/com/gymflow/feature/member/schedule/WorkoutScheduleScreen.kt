package com.gymflow.feature.member.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.feature.member.exercises.ExerciseEquipmentBadge
import com.gymflow.feature.member.exercises.ExerciseSelectorBottomSheet
import com.gymflow.feature.member.exercises.ExerciseThumbnail
import com.gymflow.feature.member.exercises.ExerciseVisuals
import com.gymflow.feature.member.exercises.MuscleCategoryChip
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
        onOpenExerciseSelector = { entry -> viewModel.openExerciseSelector(entry) },
        onCloseExerciseSelector = { viewModel.closeExerciseSelector() },
        onAddExercise = { ex, sets, reps, weight -> viewModel.addExerciseToCurrentDay(ex, sets, reps, weight) },
        onRemoveExercise = { dayName, idx -> viewModel.removeExerciseFromDay(dayName, idx) },
        onOpenDayEditor = { entry -> viewModel.openDayEditor(entry) },
        onCloseDayEditor = { viewModel.closeDayEditor() },
        onUpdateDayTitle = { dayName, title -> viewModel.updateDayTitle(dayName, title) },
        onToggleRestDay = { dayName -> viewModel.toggleRestDay(dayName) },
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
    onOpenExerciseSelector: (ScheduleWorkoutEntry) -> Unit,
    onCloseExerciseSelector: () -> Unit,
    onAddExercise: (Exercise, Int, Int, Float) -> Unit,
    onRemoveExercise: (String, Int) -> Unit,
    onOpenDayEditor: (ScheduleWorkoutEntry) -> Unit,
    onCloseDayEditor: () -> Unit,
    onUpdateDayTitle: (String, String) -> Unit,
    onToggleRestDay: (String) -> Unit,
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
                Column {
                    Text(
                        text = "Weekly Routine",
                        style = GymFlowTheme.typography.Heading2,
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Text(
                        text = "Manage & schedule your muscle groups",
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
                Text(
                    text = selectedDayFull,
                    style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = GymFlowTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Routine Entries
            uiState.scheduleEntries.forEach { entry ->
                val isCurrentSelectedDay = entry.dayName.equals(selectedDayShort, ignoreCase = true)
                ScheduleDayCard(
                    entry = entry,
                    isSelected = isCurrentSelectedDay,
                    onCardClick = {
                        val dayItem = uiState.weekDays.find { it.dayOfWeek.equals(entry.dayName, ignoreCase = true) }
                        if (dayItem != null) {
                            onSelectDay(dayItem)
                        }
                    },
                    onAddExerciseClick = { onOpenExerciseSelector(entry) },
                    onEditDayClick = { onOpenDayEditor(entry) },
                    onDeleteExercise = { idx -> onRemoveExercise(entry.dayName, idx) },
                    onSelectExercise = { exId -> onSelectWorkout(exId) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Modal: Exercise Selector Sheet
    if (uiState.isExerciseSelectorOpen) {
        ExerciseSelectorBottomSheet(
            exercises = uiState.catalogExercises,
            onDismissRequest = onCloseExerciseSelector,
            onExerciseSelected = onAddExercise
        )
    }

    // Dialog: Day Editor (Title & Rest Day toggle)
    val dayToEdit = uiState.dayBeingEdited
    if (uiState.isDayEditorOpen && dayToEdit != null) {
        DayEditorDialog(
            entry = dayToEdit,
            onDismiss = onCloseDayEditor,
            onSave = { newTitle ->
                onUpdateDayTitle(dayToEdit.dayName, newTitle)
            },
            onToggleRest = {
                onToggleRestDay(dayToEdit.dayName)
                onCloseDayEditor()
            }
        )
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
private fun ScheduleDayCard(
    entry: ScheduleWorkoutEntry,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onEditDayClick: () -> Unit,
    onDeleteExercise: (Int) -> Unit,
    onSelectExercise: (String) -> Unit
) {
    GymFlowCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = null,
        contentPadding = 14.dp,
        borderColor = if (isSelected) GymFlowBlack else GymFlowBorder,
        borderWidth = if (isSelected) 1.5.dp else 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCardClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status Circle
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
                                color = if (isSelected) GymFlowBlack else GymFlowTheme.colors.textSecondary
                            )
                            Text(
                                text = " · ${entry.title}",
                                style = GymFlowTheme.typography.BodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = GymFlowTheme.colors.textPrimary
                            )
                            if (isSelected) {
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

                        Spacer(modifier = Modifier.height(2.dp))

                        if (!entry.isRestDay) {
                            Text(
                                text = "${entry.exerciseCount} exercises · ${entry.estimatedMinutes} min",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        } else {
                            Text(
                                text = "Rest & Active Recovery",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // Edit Button
                IconButton(
                    onClick = onEditDayClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Day",
                        tint = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expanded exercise list for selected day
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = GymFlowBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (entry.isRestDay) {
                        Text(
                            text = "This day is designated as a rest day. Enjoy recovery, hydration and muscle protein synthesis.",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else if (entry.exercises.isEmpty()) {
                        Text(
                            text = "No exercises scheduled yet. Tap '+ Add Exercise' to configure from the machine catalog.",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        entry.exercises.forEachIndexed { idx, ex ->
                            ScheduledExerciseItemRow(
                                exercise = ex,
                                onDelete = { onDeleteExercise(idx) },
                                onClick = { onSelectExercise(ex.exerciseId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "+ Add Exercise" Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymFlowBlack)
                                .clickable(onClick = onAddExerciseClick)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = GymFlowWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Exercise",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite
                                )
                            }
                        }

                        // Rest Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GymFlowBorder, RoundedCornerShape(8.dp))
                                .clickable(onClick = onEditDayClick)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (entry.isRestDay) "Set Active" else "Set Rest",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GymFlowTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledExerciseItemRow(
    exercise: WorkoutExercise,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GymFlowTheme.colors.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExerciseThumbnail(
            imageUrl = ExerciseVisuals.getImageUrl(exercise.exerciseId),
            category = exercise.category,
            equipment = "Machine",
            modifier = Modifier.size(42.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = GymFlowTheme.typography.BodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = GymFlowTheme.colors.textPrimary
                )
                MuscleCategoryChip(category = exercise.category)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${exercise.targetSets} sets · ${exercise.targetReps} reps · ${exercise.targetWeightKg} kg" +
                        if (exercise.notes.isNotBlank()) " · ${exercise.notes}" else "",
                style = GymFlowTheme.typography.Caption.copy(fontSize = 11.sp),
                color = GymFlowTheme.colors.textSecondary
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove exercise",
                modifier = Modifier.size(16.dp),
                tint = GymFlowTheme.colors.textTertiary
            )
        }
    }
}

@Composable
private fun DayEditorDialog(
    entry: ScheduleWorkoutEntry,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onToggleRest: () -> Unit
) {
    var titleText by remember { mutableStateOf(entry.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit ${entry.dayOfWeekFull}",
                style = GymFlowTheme.typography.Heading2.copy(fontSize = 18.sp),
                color = GymFlowTheme.colors.textPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Workout Focus / Title",
                    style = GymFlowTheme.typography.Caption,
                    color = GymFlowTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GymFlowBlack,
                        unfocusedBorderColor = GymFlowBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GymFlowTheme.colors.surfaceVariant)
                        .clickable(onClick = onToggleRest)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (entry.isRestDay) "Convert to Training Day" else "Mark as Rest Day",
                        style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.Bold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titleText.isNotBlank()) {
                        onSave(titleText)
                    }
                }
            ) {
                Text(
                    text = "Save",
                    fontWeight = FontWeight.Bold,
                    color = GymFlowTheme.colors.textPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = GymFlowTheme.colors.textSecondary
                )
            }
        },
        containerColor = GymFlowTheme.colors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
