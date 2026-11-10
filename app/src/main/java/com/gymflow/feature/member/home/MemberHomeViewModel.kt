package com.gymflow.feature.member.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.core.database.DailyStepsDao
import com.gymflow.core.database.DailyStepsEntity
import com.gymflow.core.database.OutdoorWorkoutDao
import com.gymflow.core.database.OutdoorWorkoutEntity
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.firebase.FirestoreConnectionManager
import com.gymflow.core.firebase.FirestoreConnectionStatus
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.core.service.OutdoorTrackingService
import com.gymflow.core.service.WorkoutEngine
import com.gymflow.core.step.OutdoorWorkoutEngine
import com.gymflow.core.step.StepSensorManager
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.repository.WeightRepository
import com.gymflow.feature.member.exercises.ExerciseVisuals
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

fun getWallClockGreeting(firstName: String): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good morning, $firstName!"
        in 12..16 -> "Good afternoon, $firstName!"
        in 17..21 -> "Good evening, $firstName!"
        else -> "Late session, $firstName!"
    }
}

data class StepDayTrend(
    val dayLabel: String,
    val dateStr: String,
    val steps: Int,
    val targetGoal: Int,
    val isToday: Boolean,
    val isGoalReached: Boolean
) {
    val progressFraction: Float
        get() = (steps.toFloat() / targetGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

data class MemberHomeUiState(
    val memberName: String = "Member",
    val greeting: String = getWallClockGreeting("Member"),
    val motivationalSubtext: String = "Ready to achieve your goals?",
    val currentWeightKg: String = "0.0",
    val caloriesToday: String = "0",
    val exercisesProgress: String = "0/0",
    val workoutDurationMinutes: String = "0",
    val todayWorkoutTitle: String = "No Workout Scheduled",
    val todayExercises: List<WorkoutExercise> = emptyList(),
    val completedExerciseIds: Set<String> = emptySet(),
    val hasActiveSession: Boolean = false,
    val activeSessionDurationSeconds: Long = 0L,
    val hasUnreadNotifications: Boolean = false,
    val isLoading: Boolean = false,
    val userInitials: String = "GF",
    val isEmailVerified: Boolean = true,
    val graceDaysRemaining: Int = 7,
    val connectionStatus: FirestoreConnectionStatus = FirestoreConnectionStatus.CONNECTED,
    val todaySteps: Int = 0,
    val stepGoal: Int = 10000,
    val stepDistanceKm: Float = 0f,
    val stepCalories: Float = 0f,
    val isOutdoorTracking: Boolean = false,
    val isOutdoorPaused: Boolean = false,
    val outdoorDurationSeconds: Long = 0L,
    val outdoorDistanceMeters: Float = 0f,
    val outdoorPace: String = "--:--",
    val outdoorWorkoutType: String = "RUN",
    val recentStepsHistory: List<DailyStepsEntity> = emptyList(),
    val weeklyDayTrends: List<StepDayTrend> = emptyList(),
    val recentOutdoorWorkouts: List<OutdoorWorkoutEntity> = emptyList(),
    val weeklyAverageSteps: Int = 0,
    val weeklyTotalDistanceKm: Float = 0f,
    val daysGoalReachedCount: Int = 0,
    val currentStreakDays: Int = 0
) {
    val stepProgressFraction: Float
        get() = (todaySteps.toFloat() / stepGoal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val weightRepository: WeightRepository,
    private val planDao: PlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val firestoreConnectionManager: FirestoreConnectionManager,
    private val stepSensorManager: StepSensorManager,
    private val dailyStepsDao: DailyStepsDao,
    private val outdoorWorkoutDao: OutdoorWorkoutDao,
    private val outdoorWorkoutEngine: OutdoorWorkoutEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState: StateFlow<MemberHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeCurrentUser()
        observeLatestWeight()
        observeTodayWorkout()
        observeConnectionStatus()
        initStepCounting()
        observeStepsHistory()
        observeOutdoorTracking()
        try {
            com.gymflow.feature.widget.GymFlowWidgetUpdater.updateAllWidgets(context)
        } catch (_: Exception) {}
    }

    private fun observeConnectionStatus() {
        viewModelScope.launch {
            firestoreConnectionManager.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }
    }

    fun discardActiveSession() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"

            // 0. Cancel active workout engine and clear SharedPreferences active workout state
            try {
                WorkoutEngine.instance.cancelWorkout()
            } catch (_: Exception) {}

            // 1. Capture active session IDs before deletion
            val activeSessions = workoutSessionDao.getAllActiveSessionsSync(userId)

            // 2. Delete active set logs & active sessions from Room SQLite
            workoutSessionDao.deleteSetLogsForActiveSessions(userId)
            workoutSessionDao.discardActiveSessions(userId)

            // 3. Update UI state immediately
            _uiState.update { current ->
                current.copy(
                    hasActiveSession = false,
                    activeSessionDurationSeconds = 0L
                )
            }

            // 4. Remove session documents from Cloud Firestore
            for (s in activeSessions) {
                try {
                    firestoreSyncManager.deleteWorkoutSession(s.id)
                } catch (_: Exception) {}
            }

            // 5. Clean up any lingering incomplete sessions in Cloud Firestore
            try {
                firestoreSyncManager.deleteIncompleteSessions(userId)
            } catch (_: Exception) {}
        }
    }

    private fun extractInitials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "GF"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        }
    }

    private fun observeTodayWorkout() {
        val startOfDayMs = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            combine(
                planDao.getActivePlanWithDays(userId),
                workoutSessionDao.getCompletedExerciseIdsToday(userId, startOfDayMs),
                workoutSessionDao.getActiveSessionTodayFlow(userId, startOfDayMs)
            ) { planWithDays, completedIdsList, activeSession ->
                val completedSet = completedIdsList.toSet()
                val hasActive = activeSession != null && !activeSession.isCompleted
                val activeDuration = activeSession?.durationSeconds ?: 0L

                if (planWithDays != null) {
                    val domainPlan = planWithDays.toDomain()
                    val today = LocalDate.now()
                    val currentDayFull = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
                    val currentDayShort = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)

                    val todayDay = domainPlan.days.find { day ->
                        day.dayOfWeek.startsWith(currentDayShort, ignoreCase = true) ||
                                day.dayOfWeek.equals(currentDayFull, ignoreCase = true)
                    }

                    if (todayDay != null) {
                        if (todayDay.isRestDay) {
                            _uiState.update { current ->
                                current.copy(
                                    todayWorkoutTitle = "Rest Day · Active Recovery",
                                    todayExercises = emptyList(),
                                    exercisesProgress = "Rest",
                                    workoutDurationMinutes = "0",
                                    completedExerciseIds = emptySet(),
                                    hasActiveSession = false,
                                    activeSessionDurationSeconds = 0L
                                )
                            }
                        } else {
                            val count = todayDay.exercises.size
                            val estMinutes = (count * 10).coerceAtLeast(30)
                            val completedCount = todayDay.exercises.count { completedSet.contains(it.exerciseId) }
                            prefetchExerciseVisuals(todayDay.exercises)
                            val formattedTitle = if (count == 0) {
                                "${todayDay.title} · Rest Day"
                            } else {
                                "${todayDay.title} ($count exercises · $estMinutes min)"
                            }
                            _uiState.update { current ->
                                current.copy(
                                    todayWorkoutTitle = formattedTitle,
                                    todayExercises = todayDay.exercises,
                                    completedExerciseIds = completedSet,
                                    exercisesProgress = if (count == 0) "0/0" else "$completedCount/$count",
                                    workoutDurationMinutes = if (count == 0) "0" else "$estMinutes",
                                    hasActiveSession = hasActive,
                                    activeSessionDurationSeconds = activeDuration
                                )
                            }
                        }
                    }
                }
            }.collect { }
        }
    }

    private fun observeLatestWeight() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            weightRepository.getLatestWeight(userId).collect { latestWeight ->
                val weight = latestWeight?.postWeightKg ?: latestWeight?.preWeightKg
                if (weight != null && weight > 0f) {
                    _uiState.update { current ->
                        current.copy(
                            currentWeightKg = String.format(Locale.US, "%.1f", weight)
                        )
                    }
                }
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            // Check TokenManager cache first
            val cachedName = tokenManager.getName()
            val cachedUserId = tokenManager.getUserIdSync()
            if (!cachedName.isNullOrBlank()) {
                val firstName = cachedName.trim().split(" ").firstOrNull() ?: cachedName
                val initials = extractInitials(cachedName)
                val isVerified = tokenManager.isEmailVerifiedSync()
                val daysLeft = tokenManager.getGraceDaysRemaining()
                _uiState.update {
                    it.copy(
                        memberName = firstName,
                        greeting = getWallClockGreeting(firstName),
                        userInitials = initials,
                        isEmailVerified = isVerified,
                        graceDaysRemaining = daysLeft
                    )
                }
            }

            // Ensure user exists in Room cache
            if (!cachedUserId.isNullOrBlank()) {
                val existing = userDao.getUserById(cachedUserId)
                if (existing == null) {
                    userDao.upsertUser(
                        UserEntity(
                            id = cachedUserId,
                            name = cachedName ?: "GymFlow Member",
                            email = tokenManager.getEmail() ?: "",
                            role = tokenManager.getRoleSync() ?: Role.MEMBER
                        )
                    )
                }
            }

            // Observe dynamic database updates
            userDao.getCurrentUserFlow().collect { userEntity ->
                if (userEntity != null && userEntity.name.isNotBlank()) {
                    val firstName = userEntity.name.trim().split(" ").firstOrNull() ?: userEntity.name
                    val initials = extractInitials(userEntity.name)
                    _uiState.update { current ->
                        val displayWeight = if (userEntity.currentWeightKg > 0f) {
                            String.format(Locale.US, "%.1f", userEntity.currentWeightKg)
                        } else {
                            current.currentWeightKg
                        }
                        current.copy(
                            memberName = firstName,
                            greeting = getWallClockGreeting(firstName),
                            userInitials = initials,
                            currentWeightKg = displayWeight,
                            isEmailVerified = userEntity.isEmailVerified,
                            graceDaysRemaining = tokenManager.getGraceDaysRemaining()
                        )
                    }
                }
            }
        }
    }

    private fun loadHomeData() {
        _uiState.update {
            it.copy(
                todayWorkoutTitle = "No Workout Scheduled",
                todayExercises = emptyList(),
                isLoading = false
            )
        }
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            try {
                firestoreSyncManager.pullAllUserDataFromFirestore(userId)
                firestoreSyncManager.syncAllLocalUserData(userId)
            } catch (_: Exception) {}
        }
    }

    private fun prefetchExerciseVisuals(exercises: List<WorkoutExercise>) {
        if (exercises.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loader = Coil.imageLoader(context)
                for (ex in exercises) {
                    val gifUrl = ExerciseVisuals.getGifUrl(ex.exerciseId)
                    if (!gifUrl.isNullOrBlank()) {
                        val req = ImageRequest.Builder(context)
                            .data(gifUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()
                        loader.enqueue(req)
                    }
                    val imgUrl = ExerciseVisuals.getImageUrl(ex.exerciseId)
                    if (!imgUrl.isNullOrBlank()) {
                        val req = ImageRequest.Builder(context)
                            .data(imgUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build()
                        loader.enqueue(req)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun initStepCounting() {
        stepSensorManager.startTracking()
        val todayStr = LocalDate.now().toString()
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            dailyStepsDao.getDailyStepsFlow(userId, todayStr).collect { entity ->
                if (entity != null) {
                    _uiState.update {
                        it.copy(
                            todaySteps = entity.stepCount,
                            stepGoal = entity.targetGoal,
                            stepDistanceKm = entity.distanceMeters / 1000f,
                            stepCalories = entity.caloriesKcal.toFloat()
                        )
                    }
                }
            }
        }
    }

    private fun observeOutdoorTracking() {
        viewModelScope.launch {
            outdoorWorkoutEngine.state.collect { outdoorState ->
                _uiState.update {
                    it.copy(
                        isOutdoorTracking = outdoorState.isTracking,
                        isOutdoorPaused = outdoorState.isPaused,
                        outdoorDurationSeconds = outdoorState.durationSeconds,
                        outdoorDistanceMeters = outdoorState.distanceMeters,
                        outdoorPace = outdoorState.formattedPace,
                        outdoorWorkoutType = outdoorState.workoutType
                    )
                }
            }
        }
    }

    private fun observeStepsHistory() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            dailyStepsDao.getRecentDailySteps(userId, limit = 14).collect { recentEntities ->
                val today = LocalDate.now()
                val last7Days = (6 downTo 0).map { offset -> today.minusDays(offset.toLong()) }
                val entitiesByDate = recentEntities.associateBy { it.date }

                val trends = last7Days.map { date ->
                    val dateStr = date.toString()
                    val entity = entitiesByDate[dateStr]
                    val daySteps = entity?.stepCount ?: 0
                    val dayGoal = entity?.targetGoal ?: 10000
                    val dayLetter = date.dayOfWeek.name.take(1)
                    val isToday = date == today
                    StepDayTrend(
                        dayLabel = dayLetter,
                        dateStr = dateStr,
                        steps = daySteps,
                        targetGoal = dayGoal,
                        isToday = isToday,
                        isGoalReached = daySteps >= dayGoal && daySteps > 0
                    )
                }

                val totalSteps7 = trends.sumOf { it.steps }
                val avgSteps = totalSteps7 / 7
                val totalDistanceKm = recentEntities
                    .filter { it.date >= today.minusDays(6).toString() }
                    .sumOf { it.distanceMeters.toDouble() / 1000.0 }
                    .toFloat()
                val goalsReached = trends.count { it.isGoalReached }

                var streak = 0
                for (offset in 0..13) {
                    val d = today.minusDays(offset.toLong()).toString()
                    val e = entitiesByDate[d]
                    if (e != null && e.stepCount >= e.targetGoal && e.stepCount > 0) {
                        streak++
                    } else if (d != today.toString()) {
                        break
                    }
                }

                _uiState.update {
                    it.copy(
                        recentStepsHistory = recentEntities,
                        weeklyDayTrends = trends,
                        weeklyAverageSteps = avgSteps,
                        weeklyTotalDistanceKm = totalDistanceKm,
                        daysGoalReachedCount = goalsReached,
                        currentStreakDays = streak
                    )
                }
            }
        }

        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            outdoorWorkoutDao.getOutdoorWorkouts(userId).collect { workouts ->
                _uiState.update {
                    it.copy(recentOutdoorWorkouts = workouts.filter { w -> w.isCompleted })
                }
            }
        }
    }

    fun updateStepGoal(newGoal: Int) {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            val todayStr = LocalDate.now().toString()
            dailyStepsDao.updateTargetGoal(userId, todayStr, newGoal)
            _uiState.update { it.copy(stepGoal = newGoal) }
            val entity = dailyStepsDao.getDailyStepsSync(userId, todayStr)
            if (entity != null) {
                firestoreSyncManager.saveDailySteps(entity.copy(targetGoal = newGoal))
            }
            try {
                com.gymflow.feature.widget.GymFlowWidgetUpdater.updateStepsWidget(context)
            } catch (_: Exception) {}
        }
    }

    fun deleteOutdoorWorkout(workoutId: String) {
        viewModelScope.launch {
            outdoorWorkoutDao.deleteOutdoorWorkout(workoutId)
            outdoorWorkoutDao.deleteGpsPointsForWorkout(workoutId)
        }
    }

    fun startOutdoorTracking(workoutType: String = "RUN") {
        val serviceIntent = Intent(context, OutdoorTrackingService::class.java).apply {
            action = OutdoorTrackingService.ACTION_START
            putExtra(OutdoorTrackingService.EXTRA_WORKOUT_TYPE, workoutType)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun pauseOutdoorTracking() {
        val serviceIntent = Intent(context, OutdoorTrackingService::class.java).apply {
            action = OutdoorTrackingService.ACTION_PAUSE
        }
        context.startService(serviceIntent)
    }

    fun resumeOutdoorTracking() {
        val serviceIntent = Intent(context, OutdoorTrackingService::class.java).apply {
            action = OutdoorTrackingService.ACTION_RESUME
        }
        context.startService(serviceIntent)
    }

    fun finishOutdoorTracking() {
        val serviceIntent = Intent(context, OutdoorTrackingService::class.java).apply {
            action = OutdoorTrackingService.ACTION_FINISH
        }
        context.startService(serviceIntent)
    }
}
