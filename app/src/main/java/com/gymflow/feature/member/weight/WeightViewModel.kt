package com.gymflow.feature.member.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.common.Resource
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.calculator.WeightDifferenceCalculator
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    private fun observeWeightHistory() {
        val userId = currentUserId()
        viewModelScope.launch {
            // Also check initial user weight for baseline fallback
            val user = userDao.getUserById(userId)
            val profileWeight = user?.currentWeightKg ?: 0f

            weightRepository.getWeightHistory(userId).collect { logs ->
                if (logs.isNotEmpty()) {
                    val latest = logs.first()
                    val analysis = WeightDifferenceCalculator.analyze(latest.preWeightKg, latest.postWeightKg)

                    // Plot up to 7 most recent points chronologically
                    val points = logs.take(7).reversed().map { log ->
                        val date = Date(log.recordedAtEpochMs)
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                        val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                        WeightTrendPoint(dayLabel, log.postWeightKg, dateLabel)
                    }

                    val minWeight = (points.minOf { it.weightKg } - 1.0f).coerceAtLeast(0f)
                    val maxWeight = points.maxOf { it.weightKg } + 1.0f

                    _uiState.update { current ->
                        current.copy(
                            recentLogs = logs,
                            trendPoints = points,
                            preWeightKg = latest.preWeightKg,
                            postWeightKg = latest.postWeightKg,
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

    fun recordWeight(preKg: Float, postKg: Float, notes: String = "") {
        val userId = currentUserId()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            weightRepository.logWeight(
                memberId = userId,
                preWeightKg = preKg,
                postWeightKg = postKg,
                notes = notes
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Keep MemberProfile and Home screen synced with latest logged weight
                        val user = userDao.getUserById(userId)
                        if (user != null) {
                            userDao.updateMemberProfile(
                                id = userId,
                                heightCm = user.heightCm,
                                weightKg = postKg,
                                goal = user.fitnessGoal
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
    }
}
