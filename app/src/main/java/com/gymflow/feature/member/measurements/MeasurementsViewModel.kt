package com.gymflow.feature.member.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.common.Resource
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MeasurementDelta(
    val partName: String,
    val currentValueCm: Float,
    val deltaCm: Float
)

data class MeasurementsUiState(
    val history: List<BodyMeasurement> = emptyList(),
    val latestMeasurement: BodyMeasurement? = null,
    val deltas: List<MeasurementDelta> = emptyList(),
    val isLogDialogOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeasurementsUiState())
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    init {
        observeMeasurements()
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun observeMeasurements() {
        val userId = currentUserId()
        viewModelScope.launch {
            weightRepository.getMeasurementHistory(userId).collect { history ->
                val latest = history.firstOrNull()
                val deltas = computeDeltasForHistory(history)
                _uiState.update { current ->
                    current.copy(
                        history = history,
                        latestMeasurement = latest,
                        deltas = deltas
                    )
                }
            }
        }
    }

    fun openLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = true, errorMessage = null) }
    }

    fun closeLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = false, errorMessage = null) }
    }

    fun logNewMeasurements(
        chestCm: Float,
        waistCm: Float,
        armsCm: Float,
        thighsCm: Float,
        hipsCm: Float
    ) {
        val userId = currentUserId()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val newEntry = BodyMeasurement(
                id = UUID.randomUUID().toString(),
                memberId = userId,
                chestCm = chestCm,
                waistCm = waistCm,
                armsCm = armsCm,
                thighsCm = thighsCm,
                hipsCm = hipsCm,
                recordedAtEpochMs = System.currentTimeMillis()
            )

            weightRepository.logBodyMeasurement(newEntry).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
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

    private fun computeDeltasForHistory(history: List<BodyMeasurement>): List<MeasurementDelta> {
        if (history.isEmpty()) return emptyList()

        if (history.size < 2) {
            val latest = history.first()
            return listOf(
                MeasurementDelta("Chest", latest.chestCm, 0f),
                MeasurementDelta("Waist", latest.waistCm, 0f),
                MeasurementDelta("Arms", latest.armsCm, 0f),
                MeasurementDelta("Thighs", latest.thighsCm, 0f),
                MeasurementDelta("Hips", latest.hipsCm, 0f)
            )
        }

        val latest = history[0]
        val previous = history[1]

        return listOf(
            MeasurementDelta("Chest", latest.chestCm, Math.round((latest.chestCm - previous.chestCm) * 10f) / 10f),
            MeasurementDelta("Waist", latest.waistCm, Math.round((latest.waistCm - previous.waistCm) * 10f) / 10f),
            MeasurementDelta("Arms", latest.armsCm, Math.round((latest.armsCm - previous.armsCm) * 10f) / 10f),
            MeasurementDelta("Thighs", latest.thighsCm, Math.round((latest.thighsCm - previous.thighsCm) * 10f) / 10f),
            MeasurementDelta("Hips", latest.hipsCm, Math.round((latest.hipsCm - previous.hipsCm) * 10f) / 10f)
        )
    }
}
