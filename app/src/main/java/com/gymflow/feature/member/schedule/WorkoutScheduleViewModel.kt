package com.gymflow.feature.member.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.notification.WorkoutReminderManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.ai.AiExerciseSuggestion
import com.gymflow.domain.ai.GymFlowAiOptimizer
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.repository.ExerciseRepository
import com.gymflow.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID
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
    val dayName: String, // Mon, Tue, ...
    val dayOfWeekFull: String, // Monday, Tuesday, ...
    val title: String,
    val exerciseId: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean = false,
    val exerciseCount: Int = 4,
    val estimatedMinutes: Int = 45,
    val exercises: List<WorkoutExercise> = emptyList()
)

data class WorkoutScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonthYearLabel: String = "",
    val weekDays: List<ScheduleDayItem> = emptyList(),
    val scheduleEntries: List<ScheduleWorkoutEntry> = emptyList(),
    val isDatePickerOpen: Boolean = false,
    val isLookingAtToday: Boolean = true,
    val isExerciseSelectorOpen: Boolean = false,
    val isDayEditorOpen: Boolean = false,
    val dayBeingEdited: ScheduleWorkoutEntry? = null,
    val catalogExercises: List<Exercise> = emptyList(),
    val activePlan: WorkoutPlan? = null,
    val isSaving: Boolean = false,
    val aiSuggestions: List<AiExerciseSuggestion> = emptyList(),
    val isAiCardExpanded: Boolean = true,
    val lastAppliedSuggestionTitle: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderTimeFormatted: String = "06:30 AM",
    val completedExerciseIds: Set<String> = emptySet()
)

@HiltViewModel
class WorkoutScheduleViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val planDao: PlanDao,
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val workoutReminderManager: WorkoutReminderManager,
    private val workoutSessionDao: WorkoutSessionDao
) : ViewModel() {

    private var cachedUserEntity: UserEntity? = null

    private val _uiState = MutableStateFlow(WorkoutScheduleUiState())
    val uiState: StateFlow<WorkoutScheduleUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        loadScheduleForDate(today)
        initializeExercisesAndPlan()
    }

    private fun initializeExercisesAndPlan() {
        viewModelScope.launch {
            // 0. Observe completed exercises for today to show tick marks
            val startOfDayMs = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            launch {
                workoutSessionDao.getCompletedExerciseIdsToday(userId, startOfDayMs).collect { ids ->
                    _uiState.update { it.copy(completedExerciseIds = ids.toSet()) }
                }
            }

            // 1. Seed & observe catalog
            exerciseRepository.seedDefaultCatalogIfEmpty()
            launch {
                exerciseRepository.syncExercisesWithFirestore()
            }
            launch {
                exerciseRepository.getExercises().collect { exercises ->
                    _uiState.update { it.copy(catalogExercises = exercises) }
                }
            }

            // 2. Observe active plan for user
            launch {
                planDao.getActivePlanWithDays(userId).collect { planWithDays ->
                    if (planWithDays != null) {
                        val domainPlan = planWithDays.toDomain()
                        _uiState.update { it.copy(activePlan = domainPlan) }
                        rebuildEntriesFromPlan(domainPlan, _uiState.value.selectedDate)
                    } else {
                        // Seed initial self-managed 7-day routine
                        val defaultPlan = createDefaultSelfManagedPlan(userId)
                        workoutRepository.savePlan(defaultPlan).collect {
                            // Persisted to Room & synced
                        }
                    }
                }
            }

            // 3. Observe user profile for AI recommendations
            launch {
                userDao.getUserFlow(userId).collect { userEntity ->
                    cachedUserEntity = userEntity
                    refreshAiOptimizerSuggestions()
                }
            }

            // 4. Initialize reminder state
            val (hour, minute) = workoutReminderManager.getReminderTime()
            _uiState.update {
                it.copy(
                    reminderEnabled = workoutReminderManager.isReminderEnabled(),
                    reminderTimeFormatted = workoutReminderManager.formatTime(hour, minute)
                )
            }
        }
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

    fun openExerciseSelector(entry: ScheduleWorkoutEntry) {
        _uiState.update {
            it.copy(
                isExerciseSelectorOpen = true,
                dayBeingEdited = entry
            )
        }
    }

    fun closeExerciseSelector() {
        _uiState.update { it.copy(isExerciseSelectorOpen = false) }
    }

    fun openDayEditor(entry: ScheduleWorkoutEntry) {
        _uiState.update {
            it.copy(
                isDayEditorOpen = true,
                dayBeingEdited = entry
            )
        }
    }

    fun closeDayEditor() {
        _uiState.update {
            it.copy(
                isDayEditorOpen = false,
                dayBeingEdited = null
            )
        }
    }

    fun addExerciseToCurrentDay(exercise: Exercise, sets: Int, reps: Int, weightKg: Float) {
        val currentPlan = _uiState.value.activePlan ?: return
        val targetDayEntry = _uiState.value.dayBeingEdited ?: return

        val newExercise = WorkoutExercise(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            category = exercise.category,
            targetSets = sets,
            targetReps = reps,
            targetWeightKg = weightKg,
            restDurationSeconds = exercise.defaultRestSeconds,
            notes = exercise.machineName ?: exercise.equipment
        )

        val updatedDays = currentPlan.days.map { day ->
            if (matchesDay(day.dayOfWeek, targetDayEntry.dayName)) {
                day.copy(
                    exercises = day.exercises + newExercise,
                    isRestDay = false // Adding exercise automatically marks day active
                )
            } else {
                day
            }
        }

        savePlanInternal(currentPlan.copy(days = updatedDays))
        closeExerciseSelector()
    }

    fun removeExerciseFromDay(dayName: String, exerciseIndex: Int) {
        val currentPlan = _uiState.value.activePlan ?: return
        val updatedDays = currentPlan.days.map { day ->
            if (matchesDay(day.dayOfWeek, dayName)) {
                val updatedExs = day.exercises.toMutableList()
                if (exerciseIndex in updatedExs.indices) {
                    updatedExs.removeAt(exerciseIndex)
                }
                day.copy(exercises = updatedExs)
            } else {
                day
            }
        }
        savePlanInternal(currentPlan.copy(days = updatedDays))
    }

    fun toggleRestDay(dayName: String) {
        val currentPlan = _uiState.value.activePlan ?: return
        val updatedDays = currentPlan.days.map { day ->
            if (matchesDay(day.dayOfWeek, dayName)) {
                day.copy(isRestDay = !day.isRestDay)
            } else {
                day
            }
        }
        savePlanInternal(currentPlan.copy(days = updatedDays))
    }

    fun updateDayTitle(dayName: String, newTitle: String) {
        val currentPlan = _uiState.value.activePlan ?: return
        val updatedDays = currentPlan.days.map { day ->
            if (matchesDay(day.dayOfWeek, dayName)) {
                day.copy(title = newTitle.trim())
            } else {
                day
            }
        }
        savePlanInternal(currentPlan.copy(days = updatedDays))
        closeDayEditor()
    }

    private fun savePlanInternal(plan: WorkoutPlan) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            workoutRepository.savePlan(plan).collect {
                _uiState.update { current ->
                    current.copy(
                        activePlan = plan,
                        isSaving = false
                    )
                }
                rebuildEntriesFromPlan(plan, _uiState.value.selectedDate)
            }
        }
    }

    private fun matchesDay(dayOfWeekFullOrShort: String, targetShort: String): Boolean {
        return dayOfWeekFullOrShort.startsWith(targetShort, ignoreCase = true) ||
                targetShort.startsWith(dayOfWeekFullOrShort.take(3), ignoreCase = true)
    }

    private fun rebuildEntriesFromPlan(plan: WorkoutPlan, targetDate: LocalDate) {
        val today = LocalDate.now()
        val startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        val dayNamesShort = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dayNamesFull = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        val entries = dayNamesShort.mapIndexed { index, shortName ->
            val date = startOfWeek.plusDays(index.toLong())
            val matchingDay = plan.days.find { matchesDay(it.dayOfWeek, shortName) }

            val title = matchingDay?.title ?: defaultTitleForIndex(index)
            val isRest = matchingDay?.isRestDay ?: (index == 6)
            val exercises = matchingDay?.exercises ?: emptyList()
            val exerciseId = exercises.firstOrNull()?.exerciseId ?: "ex_chest_barbell_bench_press"
            val count = exercises.size
            val estimatedTime = if (isRest) 0 else (count * 10).coerceAtLeast(30)

            ScheduleWorkoutEntry(
                dayName = shortName,
                dayOfWeekFull = dayNamesFull[index],
                title = title,
                exerciseId = exerciseId,
                isCompleted = date.isBefore(today),
                isRestDay = isRest,
                exerciseCount = count,
                estimatedMinutes = estimatedTime,
                exercises = exercises
            )
        }

        _uiState.update {
            it.copy(scheduleEntries = entries)
        }
        refreshAiOptimizerSuggestions()
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

        _uiState.update {
            it.copy(
                selectedDate = targetDate,
                currentMonthYearLabel = monthLabel,
                weekDays = days,
                isLookingAtToday = targetDate == today
            )
        }

        val activePlan = _uiState.value.activePlan
        if (activePlan != null) {
            rebuildEntriesFromPlan(activePlan, targetDate)
        }
    }

    fun applyAiSuggestion(suggestion: AiExerciseSuggestion) {
        val currentPlan = _uiState.value.activePlan ?: return
        val selectedDayShort = _uiState.value.selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

        val updatedDays = currentPlan.days.map { day ->
            if (matchesDay(day.dayOfWeek, selectedDayShort)) {
                val updatedExercises = day.exercises.map { ex ->
                    val updatedNotes = if (suggestion.recommendedAlternative != null &&
                        (ex.exerciseName.contains("bench", ignoreCase = true) ||
                                ex.exerciseName.contains("squat", ignoreCase = true) ||
                                ex.exerciseName.contains("press", ignoreCase = true))
                    ) {
                        "AI Suggestion: ${suggestion.recommendedAlternative}"
                    } else {
                        ex.notes
                    }
                    ex.copy(
                        targetSets = suggestion.recommendedSets,
                        targetReps = suggestion.recommendedReps,
                        restDurationSeconds = suggestion.targetRestSeconds,
                        notes = updatedNotes
                    )
                }
                day.copy(exercises = updatedExercises)
            } else {
                day
            }
        }

        savePlanInternal(currentPlan.copy(days = updatedDays))
        _uiState.update { current ->
            current.copy(
                lastAppliedSuggestionTitle = suggestion.title,
                aiSuggestions = current.aiSuggestions.filter { it.id != suggestion.id }
            )
        }
    }

    fun dismissAiSuggestion(suggestionId: String) {
        _uiState.update { current ->
            current.copy(
                aiSuggestions = current.aiSuggestions.filter { it.id != suggestionId }
            )
        }
    }

    fun toggleAiCardExpanded() {
        _uiState.update { it.copy(isAiCardExpanded = !it.isAiCardExpanded) }
    }

    fun toggleDailyReminder() {
        val currentlyEnabled = workoutReminderManager.isReminderEnabled()
        val (hour, minute) = workoutReminderManager.getReminderTime()
        if (currentlyEnabled) {
            workoutReminderManager.cancelReminder()
        } else {
            workoutReminderManager.scheduleReminder(hour, minute)
        }
        _uiState.update {
            it.copy(
                reminderEnabled = !currentlyEnabled,
                reminderTimeFormatted = workoutReminderManager.formatTime(hour, minute)
            )
        }
    }

    fun refreshAiOptimizerSuggestions() {
        val user = cachedUserEntity
        val age = user?.age ?: 28
        val height = user?.heightCm ?: 175f
        val weight = user?.currentWeightKg ?: 75f
        val goal = user?.fitnessGoal ?: "General Fitness"

        val selectedDayShort = _uiState.value.selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val currentDayEntry = _uiState.value.scheduleEntries.find { matchesDay(it.dayName, selectedDayShort) }
        val dayExercises = currentDayEntry?.exercises ?: emptyList()

        val suggestions = GymFlowAiOptimizer.optimize(
            age = age,
            heightCm = height,
            weightKg = weight,
            goal = goal,
            experienceLevel = "Intermediate",
            exercises = dayExercises
        )

        val (hour, minute) = workoutReminderManager.getReminderTime()
        _uiState.update {
            it.copy(
                aiSuggestions = suggestions,
                reminderEnabled = workoutReminderManager.isReminderEnabled(),
                reminderTimeFormatted = workoutReminderManager.formatTime(hour, minute)
            )
        }
    }

    private fun defaultTitleForIndex(index: Int): String {
        return when (index) {
            0 -> "Chest + Triceps"
            1 -> "Back + Biceps"
            2 -> "Legs"
            3 -> "Shoulders"
            4 -> "Chest + Abs"
            5 -> "Back + Legs"
            else -> "Rest Day"
        }
    }

    private fun createDefaultSelfManagedPlan(userId: String): WorkoutPlan {
        return WorkoutPlan(
            id = "plan_self_$userId",
            trainerId = "self",
            memberId = userId,
            name = "Personal Weekly Routine",
            version = 1,
            isActive = true,
            days = listOf(
                WorkoutDay(
                    dayOfWeek = "Monday",
                    title = "Chest + Triceps",
                    exercises = listOf(
                        WorkoutExercise("ex_chest_barbell_bench_press", "Flat Barbell Bench Press", "Chest", 4, 8, 60f, 90, "Olympic Flat Bench Press Station"),
                        WorkoutExercise("ex_chest_incline_dumbbell_press", "Incline Dumbbell Press", "Chest", 3, 10, 22f, 60, "Adjustable Incline Bench (30° Angle)"),
                        WorkoutExercise("ex_chest_pec_deck_fly", "Seated Pec Deck Fly", "Chest", 3, 12, 45f, 45, "Pin-Loaded Pec Deck Machine"),
                        WorkoutExercise("ex_tri_rope_pushdown", "Cable Rope Triceps Pushdown", "Triceps", 3, 12, 22f, 45, "High Cable Pulley with Rope Attachment"),
                        WorkoutExercise("ex_tri_dip_machine", "Seated Triceps Dip Machine", "Triceps", 3, 12, 50f, 45, "Pin-Loaded Seated Dip Machine")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Tuesday",
                    title = "Back + Biceps",
                    exercises = listOf(
                        WorkoutExercise("ex_back_lat_pulldown", "Wide-Grip Lat Pulldown", "Back", 4, 10, 50f, 60, "Cable Lat Pulldown Column"),
                        WorkoutExercise("ex_back_seated_cable_row", "Seated Cable Row", "Back", 3, 12, 45f, 45, "Low Cable Row Station (V-Grip Handle)"),
                        WorkoutExercise("ex_back_barbell_bent_row", "Barbell Bent-Over Row", "Back", 3, 8, 50f, 60, "Olympic Barbell & Bumper Plates"),
                        WorkoutExercise("ex_bi_barbell_curl", "Standing Barbell Bicep Curl", "Biceps", 3, 10, 25f, 45, "EZ-Curl Bar or Olympic Barbell"),
                        WorkoutExercise("ex_bi_preacher_curl_machine", "Preacher Curl Machine", "Biceps", 3, 12, 30f, 45, "Pin-Loaded Preacher Curl Station")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Wednesday",
                    title = "Legs",
                    exercises = listOf(
                        WorkoutExercise("ex_leg_barbell_squat", "Barbell Back Squat", "Legs", 4, 8, 80f, 90, "Power Rack / Squat Cage & Olympic Barbell"),
                        WorkoutExercise("ex_leg_incline_leg_press", "45-Degree Incline Leg Press", "Legs", 4, 12, 120f, 60, "Plate-Loaded 45° Sled Leg Press"),
                        WorkoutExercise("ex_leg_quad_extension", "Leg Extension Machine", "Legs", 3, 12, 45f, 45, "Pin-Loaded Leg Extension Machine"),
                        WorkoutExercise("ex_leg_lying_hamstring_curl", "Lying Hamstring Curl", "Legs", 3, 12, 40f, 45, "Pin-Loaded Prone Leg Curl Machine"),
                        WorkoutExercise("ex_leg_standing_calf_raise", "Standing Calf Raise Machine", "Legs", 4, 15, 65f, 45, "Pin-Loaded Standing Calf Machine")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Thursday",
                    title = "Shoulders",
                    exercises = listOf(
                        WorkoutExercise("ex_sh_overhead_press", "Standing Overhead Barbell Press", "Shoulders", 4, 8, 40f, 60, "Squat Stand / Power Rack & Barbell"),
                        WorkoutExercise("ex_sh_seated_dumbbell_press", "Seated Dumbbell Shoulder Press", "Shoulders", 3, 10, 18f, 45, "90° Upright Utility Bench"),
                        WorkoutExercise("ex_sh_cable_lateral_raise", "Cable Lateral Raise", "Shoulders", 3, 15, 8f, 45, "Low Pulley Cable Column (Single Handle)"),
                        WorkoutExercise("ex_sh_reverse_pec_deck", "Reverse Pec Deck (Rear Delts)", "Shoulders", 3, 15, 35f, 45, "Dual Rear Delt / Pec Deck Machine")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Friday",
                    title = "Chest + Abs",
                    exercises = listOf(
                        WorkoutExercise("ex_chest_cable_crossover", "Standing Cable Chest Fly", "Chest", 3, 15, 15f, 45, "Dual Adjustable Cable Column"),
                        WorkoutExercise("ex_chest_hammer_strength_press", "Hammer Strength Incline Press", "Chest", 3, 10, 35f, 60, "Plate-Loaded Incline Chest Press"),
                        WorkoutExercise("ex_abs_cable_rope_crunch", "Kneeling Cable Rope Crunch", "Abs", 3, 15, 25f, 45, "High Cable Column with Rope Attachment"),
                        WorkoutExercise("ex_abs_hanging_leg_raise", "Hanging Leg Raise", "Abs", 3, 12, 0f, 45, "Pull-Up Rig / Captain's Chair Station")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Saturday",
                    title = "Back + Legs",
                    exercises = listOf(
                        WorkoutExercise("ex_back_deadlift", "Conventional Barbell Deadlift", "Back", 4, 6, 90f, 90, "Olympic Lifting Platform & Barbell"),
                        WorkoutExercise("ex_leg_romanian_deadlift", "Romanian Deadlift (RDL)", "Legs", 3, 10, 60f, 60, "Olympic Barbell / Dumbbell Set"),
                        WorkoutExercise("ex_back_tbar_row", "Chest-Supported T-Bar Row", "Back", 3, 10, 35f, 60, "Plate-Loaded T-Bar Row Machine"),
                        WorkoutExercise("ex_leg_hack_squat", "Hack Squat Machine", "Legs", 3, 10, 70f, 60, "Plate-Loaded Hack Squat Sled")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Sunday",
                    title = "Rest Day",
                    exercises = emptyList(),
                    isRestDay = true
                )
            )
        )
    }
}
