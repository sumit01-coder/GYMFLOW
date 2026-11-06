package com.gymflow.feature.member.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
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
    val graceDaysRemaining: Int = 7
)

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val weightRepository: WeightRepository,
    private val planDao: PlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val firestoreSyncManager: FirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState: StateFlow<MemberHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeCurrentUser()
        observeLatestWeight()
        observeTodayWorkout()
    }

    fun discardActiveSession() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            workoutSessionDao.discardActiveSessions(userId)
            _uiState.update { current ->
                current.copy(
                    hasActiveSession = false,
                    activeSessionDurationSeconds = 0L
                )
            }
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
                                "${todayDay.title} (0 exercises · Rest / Free Day)"
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
}
