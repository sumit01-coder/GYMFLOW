package com.gymflow.feature.trainer.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.GymMemberDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
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

enum class ProgressTimeframe {
    D7,
    D30,
    M3,
    M6,
    Y1
}

data class WeightDataPoint(
    val dateLabel: String,
    val weightKg: Float
)

data class WorkoutSessionLog(
    val sessionDate: String,
    val workoutName: String,
    val totalVolumeKg: Float,
    val durationMinutes: Int,
    val coachApprovalStatus: String // Approved, Under Review
)

data class MemberProgressUiState(
    val memberId: String = "",
    val memberName: String = "",
    val selectedTimeframe: ProgressTimeframe = ProgressTimeframe.D30,
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val totalCalories: Int = 0,
    val bestBenchPressKg: Float = 0f,
    val startingWeightKg: Float = 0f,
    val currentWeightKg: Float = 0f,
    val weightTrendPoints: List<WeightDataPoint> = emptyList(),
    val recentSessions: List<WorkoutSessionLog> = emptyList(),
    val coachFeedbackInput: String = "",
    val feedbackSubmittedToast: Boolean = false
)

@HiltViewModel
class TrainerMemberProgressViewModel(
    private val userDao: UserDao?,
    private val workoutSessionDao: WorkoutSessionDao?,
    private val weightDao: WeightDao?,
    private val gymMemberDao: GymMemberDao?,
    @Suppress("UNUSED_PARAMETER") private val isHilt: Boolean
) : ViewModel() {

    @Inject
    constructor(
        userDao: UserDao,
        workoutSessionDao: WorkoutSessionDao,
        weightDao: WeightDao,
        gymMemberDao: GymMemberDao
    ) : this(userDao, workoutSessionDao, weightDao, gymMemberDao, true)

    constructor() : this(null, null, null, null, false)

    private val _uiState = MutableStateFlow(MemberProgressUiState())
    val uiState: StateFlow<MemberProgressUiState> = _uiState.asStateFlow()
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())

    fun loadMemberData(memberId: String) {
        _uiState.update { it.copy(memberId = memberId) }

        val uDao = userDao ?: return
        val wsDao = workoutSessionDao ?: return
        val wDao = weightDao ?: return
        val gmDao = gymMemberDao ?: return

        viewModelScope.launch {
            val user = uDao.getUserById(memberId)
            val gymMember = gmDao.getMemberById(memberId)
            val resolvedName = user?.name?.ifBlank { null }
                ?: gymMember?.name?.ifBlank { null }
                ?: "Athlete"

            _uiState.update { it.copy(memberName = resolvedName) }

            combine(
                wsDao.getAllSessionsWithSets(memberId),
                wDao.getWeightHistory(memberId)
            ) { sessionsWithSets, weightLogs ->
                val totalWorkouts = sessionsWithSets.size
                val totalSets = sessionsWithSets.sumOf { it.setLogs.size }
                val totalCalories = sessionsWithSets.sumOf { it.session.caloriesBurned }

                // Best bench press or chest lift
                val bestBench = sessionsWithSets.flatMap { it.setLogs }
                    .filter { it.exerciseName.contains("bench", ignoreCase = true) }
                    .maxOfOrNull { it.actualWeightKg } ?: 0f

                // Weight trends from database
                val sortedWeights = weightLogs.sortedBy { it.recordedAtEpochMs }
                val startWeight = sortedWeights.firstOrNull()?.let { if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg } ?: user?.currentWeightKg ?: 0f
                val currWeight = sortedWeights.lastOrNull()?.let { if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg } ?: user?.currentWeightKg ?: 0f

                val trendPoints = sortedWeights.map {
                    val w = if (it.postWeightKg > 0f) it.postWeightKg else it.preWeightKg
                    WeightDataPoint(
                        dateLabel = shortDateFormatter.format(Date(it.recordedAtEpochMs)),
                        weightKg = w
                    )
                }

                // Recent completed workout sessions
                val sessionLogs = sessionsWithSets.map { sws ->
                    val sess = sws.session
                    WorkoutSessionLog(
                        sessionDate = dateFormatter.format(Date(sess.startTimeEpochMs)),
                        workoutName = sess.workoutTitle,
                        totalVolumeKg = sess.totalVolumeKg,
                        durationMinutes = (sess.durationSeconds / 60).toInt().coerceAtLeast(1),
                        coachApprovalStatus = if (sess.isCompleted) "Verified Session" else "In Progress"
                    )
                }

                _uiState.update { current ->
                    current.copy(
                        totalWorkouts = totalWorkouts,
                        totalSets = totalSets,
                        totalCalories = totalCalories,
                        bestBenchPressKg = bestBench,
                        startingWeightKg = startWeight,
                        currentWeightKg = currWeight,
                        weightTrendPoints = trendPoints,
                        recentSessions = sessionLogs
                    )
                }
            }.collect {}
        }
    }

    fun selectTimeframe(timeframe: ProgressTimeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun onCoachFeedbackInputChanged(text: String) {
        _uiState.update { it.copy(coachFeedbackInput = text, feedbackSubmittedToast = false) }
    }

    fun submitFeedback() {
        if (_uiState.value.coachFeedbackInput.isBlank()) return
        _uiState.update {
            it.copy(
                coachFeedbackInput = "",
                feedbackSubmittedToast = true
            )
        }
    }
}
