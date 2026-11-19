package com.gymflow.feature.trainer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.GymMemberDao
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.security.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class RoutineDayStatus(
    val dayOfWeek: String,
    val routineTitle: String,
    val isCompleted: Boolean,
    val isToday: Boolean = false,
    val isRestDay: Boolean = false
)

data class WeightHistoryItem(
    val date: String,
    val weightKg: Float,
    val deltaKg: Float
)

data class CoachNoteItem(
    val id: String,
    val timestamp: String,
    val text: String,
    val author: String = "Coach"
)

data class MemberProfileDetails(
    val memberId: String = "",
    val name: String = "",
    val email: String = "",
    val status: String = "Active",
    val joinedDate: String = "",
    val goal: String = "General Fitness",
    val currentWeightKg: Float = 0f,
    val startingWeightKg: Float = 0f,
    val heightCm: Float = 0f,
    val age: Int = 0,
    val attendanceRate: Int = 0,
    val workoutsCompletedThisMonth: Int = 0,
    val routineTitle: String = "No Plan Assigned",
    val weeklyDays: List<RoutineDayStatus> = emptyList(),
    val weightHistory: List<WeightHistoryItem> = emptyList(),
    val notesHistory: List<CoachNoteItem> = emptyList()
)

data class TrainerMemberProfileUiState(
    val profile: MemberProfileDetails = MemberProfileDetails(),
    val feedbackInput: String = "",
    val isNoteSentNoticeVisible: Boolean = false
)

@HiltViewModel
class TrainerMemberProfileViewModel(
    private val userDao: UserDao?,
    private val gymMemberDao: GymMemberDao?,
    private val planDao: PlanDao?,
    private val weightDao: WeightDao?,
    private val workoutSessionDao: WorkoutSessionDao?,
    private val tokenManager: TokenManager?,
    @Suppress("UNUSED_PARAMETER") private val isHilt: Boolean
) : ViewModel() {

    @Inject
    constructor(
        userDao: UserDao,
        gymMemberDao: GymMemberDao,
        planDao: PlanDao,
        weightDao: WeightDao,
        workoutSessionDao: WorkoutSessionDao,
        tokenManager: TokenManager
    ) : this(userDao, gymMemberDao, planDao, weightDao, workoutSessionDao, tokenManager, true)

    constructor() : this(null, null, null, null, null, null, false)

    private val _uiState = MutableStateFlow(TrainerMemberProfileUiState())
    val uiState: StateFlow<TrainerMemberProfileUiState> = _uiState.asStateFlow()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

    fun loadMemberProfile(memberId: String) {
        val uDao = userDao ?: return
        val gmDao = gymMemberDao ?: return
        val pDao = planDao ?: return
        val wDao = weightDao ?: return
        val wsDao = workoutSessionDao ?: return

        viewModelScope.launch {
            val user = uDao.getUserById(memberId)
            val gymMember = gmDao.getMemberById(memberId)

            val name = user?.name?.ifBlank { null } ?: gymMember?.name?.ifBlank { null } ?: "Athlete"
            val email = user?.email?.ifBlank { null } ?: gymMember?.email?.ifBlank { null } ?: ""
            val goal = user?.fitnessGoal?.ifBlank { null } ?: gymMember?.notes?.ifBlank { null } ?: "General Fitness"
            val joinedDate = "Joined " + dateFormatter.format(Date(user?.createdAtEpochMs ?: gymMember?.startDateEpochMs ?: System.currentTimeMillis()))

            val initialProfile = _uiState.value.profile.copy(
                memberId = memberId,
                name = name,
                email = email,
                goal = goal,
                joinedDate = joinedDate,
                heightCm = user?.heightCm ?: 0f,
                age = user?.age ?: 0,
                currentWeightKg = user?.currentWeightKg ?: 0f,
                status = if (gymMember != null && gymMember.endDateEpochMs < System.currentTimeMillis()) "Expired" else "Active"
            )
            _uiState.update { it.copy(profile = initialProfile) }

            combine(
                wDao.getWeightHistory(memberId),
                wsDao.getAllSessionsWithSets(memberId),
                pDao.getActivePlanWithDays(memberId)
            ) { weights, sessions, activePlanWithDays ->
                val activePlan = activePlanWithDays?.toDomain()
                val sortedWeights = weights.sortedByDescending { it.recordedAtEpochMs }
                val startWeight = sortedWeights.lastOrNull()?.let { if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg } ?: user?.currentWeightKg ?: 0f
                val currWeight = sortedWeights.firstOrNull()?.let { if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg } ?: user?.currentWeightKg ?: 0f

                val weightHistory = sortedWeights.mapIndexed { idx, item ->
                    val itemW = if (item.postWeightKg > 0f) item.postWeightKg else item.preWeightKg
                    val nextItem = sortedWeights.getOrNull(idx + 1)
                    val nextWeight = nextItem?.let { if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg } ?: itemW
                    WeightHistoryItem(
                        date = shortDateFormatter.format(Date(item.recordedAtEpochMs)),
                        weightKg = itemW,
                        deltaKg = itemW - nextWeight
                    )
                }

                val currentMonth = java.time.LocalDate.now().monthValue
                val sessionsThisMonth = sessions.count {
                    val date = java.time.Instant.ofEpochMilli(it.session.startTimeEpochMs)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    date.monthValue == currentMonth && it.session.isCompleted
                }

                val weeklyDays = activePlan?.days?.map { day ->
                    val isToday = day.dayOfWeek.equals(java.time.LocalDate.now().dayOfWeek.name, ignoreCase = true)
                    RoutineDayStatus(
                        dayOfWeek = day.dayOfWeek.take(3),
                        routineTitle = day.title,
                        isCompleted = day.isCompleted,
                        isToday = isToday,
                        isRestDay = day.isRestDay
                    )
                } ?: emptyList()

                _uiState.update { current ->
                    val p = current.profile.copy(
                        startingWeightKg = startWeight,
                        currentWeightKg = currWeight,
                        workoutsCompletedThisMonth = sessionsThisMonth,
                        attendanceRate = if (sessionsThisMonth > 0) ((sessionsThisMonth / 12f) * 100).toInt().coerceAtMost(100) else 0,
                        routineTitle = activePlan?.name ?: "No Plan Assigned",
                        weeklyDays = weeklyDays,
                        weightHistory = weightHistory
                    )
                    current.copy(profile = p)
                }
            }.collect {}
        }
    }

    fun onFeedbackInputChanged(text: String) {
        _uiState.update { it.copy(feedbackInput = text, isNoteSentNoticeVisible = false) }
    }

    fun applyPresetNote(preset: String) {
        _uiState.update { it.copy(feedbackInput = preset) }
    }

    fun sendFeedbackNote() {
        val noteText = _uiState.value.feedbackInput.trim()
        if (noteText.isBlank()) return

        val authorName = tokenManager?.getName() ?: "Coach"
        val newNote = CoachNoteItem(
            id = "note-${System.currentTimeMillis()}",
            timestamp = "Just now",
            text = noteText,
            author = if (authorName.startsWith("Coach", ignoreCase = true)) authorName else "Coach $authorName"
        )
        _uiState.update {
            it.copy(
                profile = it.profile.copy(
                    notesHistory = listOf(newNote) + it.profile.notesHistory
                ),
                feedbackInput = "",
                isNoteSentNoticeVisible = true
            )
        }
    }

    fun dismissNoteNotice() {
        _uiState.update { it.copy(isNoteSentNoticeVisible = false) }
    }
}
