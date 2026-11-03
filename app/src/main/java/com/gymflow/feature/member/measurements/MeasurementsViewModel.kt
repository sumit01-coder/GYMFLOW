package com.gymflow.feature.member.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.BodyMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MeasurementDelta(
    val partName: String,
    val currentValueCm: Float,
    val deltaCm: Float
)

data class MeasurementsUiState(
    val history: List<BodyMeasurement> = defaultMeasurementsHistory,
    val latestMeasurement: BodyMeasurement? = defaultMeasurementsHistory.firstOrNull(),
    val deltas: List<MeasurementDelta> = emptyList(),
    val isLogDialogOpen: Boolean = false
) {
    companion object {
        val defaultMeasurementsHistory = listOf(
            BodyMeasurement(
                id = "bm-1",
                memberId = "alex-1",
                chestCm = 104.5f,
                waistCm = 81.0f,
                armsCm = 38.5f,
                thighsCm = 58.0f,
                hipsCm = 96.0f,
                recordedAtEpochMs = System.currentTimeMillis() - 86400000L * 2
            ),
            BodyMeasurement(
                id = "bm-2",
                memberId = "alex-1",
                chestCm = 103.5f,
                waistCm = 82.0f,
                armsCm = 38.0f,
                thighsCm = 57.5f,
                hipsCm = 96.5f,
                recordedAtEpochMs = System.currentTimeMillis() - 86400000L * 16
            ),
            BodyMeasurement(
                id = "bm-3",
                memberId = "alex-1",
                chestCm = 102.0f,
                waistCm = 83.5f,
                armsCm = 37.0f,
                thighsCm = 56.5f,
                hipsCm = 97.0f,
                recordedAtEpochMs = System.currentTimeMillis() - 86400000L * 30
            )
        )
    }
}

class MeasurementsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MeasurementsUiState())
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    init {
        computeDeltas()
    }

    fun openLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = true) }
    }

    fun closeLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = false) }
    }

    fun logNewMeasurements(
        chestCm: Float,
        waistCm: Float,
        armsCm: Float,
        thighsCm: Float,
        hipsCm: Float
    ) {
        viewModelScope.launch {
            val newEntry = BodyMeasurement(
                id = UUID.randomUUID().toString(),
                memberId = "alex-1",
                chestCm = chestCm,
                waistCm = waistCm,
                armsCm = armsCm,
                thighsCm = thighsCm,
                hipsCm = hipsCm,
                recordedAtEpochMs = System.currentTimeMillis()
            )

            _uiState.update { current ->
                val newHistory = listOf(newEntry) + current.history
                current.copy(
                    history = newHistory,
                    latestMeasurement = newEntry,
                    isLogDialogOpen = false
                )
            }
            computeDeltas()
        }
    }

    private fun computeDeltas() {
        val history = _uiState.value.history
        if (history.size < 2) {
            val latest = history.firstOrNull() ?: return
            _uiState.update {
                it.copy(
                    deltas = listOf(
                        MeasurementDelta("Chest", latest.chestCm, 0f),
                        MeasurementDelta("Waist", latest.waistCm, 0f),
                        MeasurementDelta("Arms", latest.armsCm, 0f),
                        MeasurementDelta("Thighs", latest.thighsCm, 0f),
                        MeasurementDelta("Hips", latest.hipsCm, 0f)
                    )
                )
            }
            return
        }

        val latest = history[0]
        val previous = history[1]

        val deltas = listOf(
            MeasurementDelta("Chest", latest.chestCm, Math.round((latest.chestCm - previous.chestCm) * 10f) / 10f),
            MeasurementDelta("Waist", latest.waistCm, Math.round((latest.waistCm - previous.waistCm) * 10f) / 10f),
            MeasurementDelta("Arms", latest.armsCm, Math.round((latest.armsCm - previous.armsCm) * 10f) / 10f),
            MeasurementDelta("Thighs", latest.thighsCm, Math.round((latest.thighsCm - previous.thighsCm) * 10f) / 10f),
            MeasurementDelta("Hips", latest.hipsCm, Math.round((latest.hipsCm - previous.hipsCm) * 10f) / 10f)
        )

        _uiState.update { it.copy(deltas = deltas) }
    }
}
