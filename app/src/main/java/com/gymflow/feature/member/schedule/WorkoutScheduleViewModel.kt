package com.gymflow.feature.member.schedule

import androidx.lifecycle.ViewModel
import com.gymflow.domain.model.WorkoutDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ScheduleDayItem(
    val dayOfWeek: String, // Mon, Tue, Wed, ...
    val dayNumber: Int,    // 12, 13, 14, ...
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
    val selectedDayIndex: Int = 0,
    val weekDays: List<ScheduleDayItem> = emptyList(),
    val scheduleEntries: List<ScheduleWorkoutEntry> = emptyList()
)

@HiltViewModel
class WorkoutScheduleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutScheduleUiState())
    val uiState: StateFlow<WorkoutScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        val days = listOf(
            ScheduleDayItem("Mon", 12, isSelected = true, isCompleted = true),
            ScheduleDayItem("Tue", 13, isSelected = false, isCompleted = true),
            ScheduleDayItem("Wed", 14, isSelected = false, isCompleted = true),
            ScheduleDayItem("Thu", 15, isSelected = false, isCompleted = false),
            ScheduleDayItem("Fri", 16, isSelected = false, isCompleted = false),
            ScheduleDayItem("Sat", 17, isSelected = false, isCompleted = false),
            ScheduleDayItem("Sun", 18, isSelected = false, isCompleted = false)
        )

        val entries = listOf(
            ScheduleWorkoutEntry("Mon", "Chest + Triceps", isCompleted = true, exerciseCount = 5, estimatedMinutes = 50),
            ScheduleWorkoutEntry("Tue", "Back + Biceps", isCompleted = true, exerciseCount = 5, estimatedMinutes = 45),
            ScheduleWorkoutEntry("Wed", "Legs", isCompleted = true, exerciseCount = 6, estimatedMinutes = 60),
            ScheduleWorkoutEntry("Thu", "Shoulders", isCompleted = false, exerciseCount = 4, estimatedMinutes = 40),
            ScheduleWorkoutEntry("Fri", "Chest + Abs", isCompleted = false, exerciseCount = 5, estimatedMinutes = 45),
            ScheduleWorkoutEntry("Sat", "Back + Legs", isCompleted = false, exerciseCount = 5, estimatedMinutes = 55),
            ScheduleWorkoutEntry("Sun", "Rest Day", isCompleted = false, isRestDay = true, exerciseCount = 0, estimatedMinutes = 0)
        )

        _uiState.update {
            it.copy(
                selectedDayIndex = 0,
                weekDays = days,
                scheduleEntries = entries
            )
        }
    }

    fun onSelectDay(index: Int) {
        _uiState.update { current ->
            val updatedWeekDays = current.weekDays.mapIndexed { idx, item ->
                item.copy(isSelected = idx == index)
            }
            current.copy(
                selectedDayIndex = index,
                weekDays = updatedWeekDays
            )
        }
    }
}
