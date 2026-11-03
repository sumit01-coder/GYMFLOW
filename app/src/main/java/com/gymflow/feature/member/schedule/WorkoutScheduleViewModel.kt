package com.gymflow.feature.member.schedule

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

data class ScheduleDayItem(
    val date: LocalDate,
    val dayOfWeek: String, // Mon, Tue, Wed, ...
    val dayNumber: Int,    // 1, 2, 3, ...
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isCompleted: Boolean = false
)

data class ScheduleWorkoutEntry(
    val dayName: String,
    val title: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean = false,
    val exerciseCount: Int = 4,
    val estimatedMinutes: Int = 45
)

data class WorkoutScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonthYearLabel: String = "",
    val weekDays: List<ScheduleDayItem> = emptyList(),
    val scheduleEntries: List<ScheduleWorkoutEntry> = emptyList(),
    val isDatePickerOpen: Boolean = false,
    val isLookingAtToday: Boolean = true
)

@HiltViewModel
class WorkoutScheduleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutScheduleUiState())
    val uiState: StateFlow<WorkoutScheduleUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        loadScheduleForDate(today)
    }

    fun selectDate(date: LocalDate) {
        loadScheduleForDate(date)
    }

    fun setDatePickerOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isDatePickerOpen = isOpen) }
    }

    fun jumpToToday() {
        loadScheduleForDate(LocalDate.now())
    }

    private fun loadScheduleForDate(targetDate: LocalDate) {
        val today = LocalDate.now()
        val startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val days = (0L..6L).map { offset ->
            val date = startOfWeek.plusDays(offset)
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            ScheduleDayItem(
                date = date,
                dayOfWeek = dayOfWeek,
                dayNumber = date.dayOfMonth,
                isToday = date == today,
                isSelected = date == targetDate,
                isCompleted = date.isBefore(today)
            )
        }

        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        val monthLabel = targetDate.format(monthFormatter)

        val routine = listOf(
            ScheduleWorkoutEntry("Mon", "Chest + Triceps", isCompleted = startOfWeek.isBefore(today), exerciseCount = 5, estimatedMinutes = 50),
            ScheduleWorkoutEntry("Tue", "Back + Biceps", isCompleted = startOfWeek.plusDays(1).isBefore(today), exerciseCount = 5, estimatedMinutes = 45),
            ScheduleWorkoutEntry("Wed", "Legs", isCompleted = startOfWeek.plusDays(2).isBefore(today), exerciseCount = 6, estimatedMinutes = 60),
            ScheduleWorkoutEntry("Thu", "Shoulders", isCompleted = startOfWeek.plusDays(3).isBefore(today), exerciseCount = 4, estimatedMinutes = 40),
            ScheduleWorkoutEntry("Fri", "Chest + Abs", isCompleted = startOfWeek.plusDays(4).isBefore(today), exerciseCount = 5, estimatedMinutes = 45),
            ScheduleWorkoutEntry("Sat", "Back + Legs", isCompleted = startOfWeek.plusDays(5).isBefore(today), exerciseCount = 5, estimatedMinutes = 55),
            ScheduleWorkoutEntry("Sun", "Rest Day", isCompleted = startOfWeek.plusDays(6).isBefore(today), isRestDay = true, exerciseCount = 0, estimatedMinutes = 0)
        )

        _uiState.update {
            it.copy(
                selectedDate = targetDate,
                currentMonthYearLabel = monthLabel,
                weekDays = days,
                scheduleEntries = routine,
                isLookingAtToday = targetDate == today
            )
        }
    }
}
