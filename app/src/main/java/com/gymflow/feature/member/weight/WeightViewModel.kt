package com.gymflow.feature.member.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.common.Resource
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.calculator.WeightDifferenceCalculator
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class WeightTimeFilter(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class WeightTrendPoint(
    val dayLabel: String,
    val weightKg: Float,
    val dateString: String
)

data class WeightUiState(
    val selectedFilter: WeightTimeFilter = WeightTimeFilter.DAILY,
    val preWeightKg: Float = 0f,
    val postWeightKg: Float = 0f,
    val preRecordedTimeStr: String? = null,
    val postRecordedTimeStr: String? = null,
    val todayLog: WeightLog? = null,
    val hasPreLoggedToday: Boolean = false,
    val hasPostLoggedToday: Boolean = false,
    val deltaAnalysis: WeightDifferenceCalculator.WeightDeltaAnalysis =
        WeightDifferenceCalculator.analyze(0f, 0f),
    val trendPoints: List<WeightTrendPoint> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val recentLogs: List<WeightLog> = emptyList(),
    val isLogDialogOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        observeWeightHistory()
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun isToday(epochMs: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = epochMs }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun observeWeightHistory() {
        val userId = currentUserId()
        viewModelScope.launch {
            // Also check initial user weight for baseline fallback
            val user = userDao.getUserById(userId)
            val profileWeight = user?.currentWeightKg ?: 0f

            weightRepository.getWeightHistory(userId).collect { logs ->
                if (logs.isNotEmpty()) {
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val todayLog = logs.firstOrNull { isToday(it.recordedAtEpochMs) }
                    val displayLog = todayLog ?: logs.first()

                    val preWeight = displayLog.preWeightKg
                    val postWeight = displayLog.postWeightKg

                    val preTimeStr = timeFormat.format(Date(displayLog.recordedAtEpochMs))
                    val postTimeStr = displayLog.postRecordedAtEpochMs?.let { timeFormat.format(Date(it)) }
                        ?: if (displayLog.postWeightKg > 0f) preTimeStr else null

                    val analysis = if (displayLog.postWeightKg > 0f) {
                        WeightDifferenceCalculator.analyze(preWeight, displayLog.postWeightKg)
                    } else {
                        WeightDifferenceCalculator.analyze(preWeight, preWeight)
                    }

                    // Plot up to 7 most recent points chronologically
                    val points = logs.take(7).reversed().map { log ->
                        val date = Date(log.recordedAtEpochMs)
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                        val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                        val plotWeight = if (log.postWeightKg > 0f) log.postWeightKg else log.preWeightKg
                        WeightTrendPoint(dayLabel, plotWeight, dateLabel)
                    }

                    val minWeight = (points.minOf { it.weightKg } - 1.0f).coerceAtLeast(0f)
                    val maxWeight = points.maxOf { it.weightKg } + 1.0f

                    _uiState.update { current ->
                        current.copy(
                            recentLogs = logs,
                            todayLog = todayLog,
                            hasPreLoggedToday = todayLog != null,
                            hasPostLoggedToday = todayLog != null && todayLog.postWeightKg > 0f,
                            trendPoints = points,
                            preWeightKg = preWeight,
                            postWeightKg = postWeight,
                            preRecordedTimeStr = preTimeStr,
                            postRecordedTimeStr = postTimeStr,
                            deltaAnalysis = analysis,
                            minY = Math.round(minWeight * 10f) / 10f,
                            maxY = Math.round(maxWeight * 10f) / 10f
                        )
                    }
                } else if (profileWeight > 0f) {
                    _uiState.update { current ->
                        current.copy(
                            preWeightKg = profileWeight,
                            postWeightKg = profileWeight,
                            minY = (profileWeight - 5f).coerceAtLeast(0f),
                            maxY = profileWeight + 5f
                        )
                    }
                }
            }
        }
    }

    fun onFilterSelected(filter: WeightTimeFilter) {
        _uiState.update { current ->
            current.copy(selectedFilter = filter)
        }
    }

    fun openLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = true, errorMessage = null) }
    }

    fun closeLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = false, errorMessage = null) }
    }

    fun recordPreWeight(preKg: Float, notes: String = "") {
        val userId = currentUserId()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            weightRepository.logWeight(
                memberId = userId,
                preWeightKg = preKg,
                postWeightKg = 0f,
                notes = notes,
                recordedAtEpochMs = System.currentTimeMillis()
            ).collect { resource ->
                handleWeightResult(resource, preKg)
            }
        }
    }

    fun recordPostWeight(postKg: Float, notes: String = "") {
        val userId = currentUserId()
        val todayLog = _uiState.value.todayLog
        val preKg = todayLog?.preWeightKg ?: _uiState.value.preWeightKg
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            weightRepository.logWeight(
                memberId = userId,
                preWeightKg = preKg,
                postWeightKg = postKg,
                notes = notes,
                existingLogId = todayLog?.id,
                recordedAtEpochMs = todayLog?.recordedAtEpochMs,
                postRecordedAtEpochMs = System.currentTimeMillis()
            ).collect { resource ->
                handleWeightResult(resource, postKg)
            }
        }
    }

    fun recordWeight(preKg: Float, postKg: Float, notes: String = "") {
        val userId = currentUserId()
        val todayLog = _uiState.value.todayLog
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            weightRepository.logWeight(
                memberId = userId,
                preWeightKg = preKg,
                postWeightKg = postKg,
                notes = notes,
                existingLogId = todayLog?.id,
                recordedAtEpochMs = todayLog?.recordedAtEpochMs ?: System.currentTimeMillis(),
                postRecordedAtEpochMs = System.currentTimeMillis()
            ).collect { resource ->
                handleWeightResult(resource, postKg)
            }
        }
    }

    private suspend fun handleWeightResult(resource: Resource<WeightLog>, updatedWeightKg: Float) {
        val userId = currentUserId()
        when (resource) {
            is Resource.Success -> {
                // Keep MemberProfile and Home screen synced with latest logged weight
                val existingUser = userDao.getUserById(userId) ?: userDao.getCurrentUser()
                if (existingUser != null) {
                    userDao.updateMemberProfile(
                        id = existingUser.id,
                        heightCm = existingUser.heightCm,
                        weightKg = updatedWeightKg,
                        goal = existingUser.fitnessGoal
                    )
                } else {
                    userDao.upsertUser(
                        UserEntity(
                            id = userId,
                            name = tokenManager.getName() ?: "GymFlow Member",
                            email = tokenManager.getEmail() ?: "",
                            role = Role.MEMBER,
                            currentWeightKg = updatedWeightKg
                        )
                    )
                }
                _uiState.update { it.copy(isSaving = false, isLogDialogOpen = false) }
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
            }
            is Resource.Loading -> {
                _uiState.update { it.copy(isSaving = true) }
            }
        }
    }
}
