package com.gymflow.feature.trainer.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MuscleDistribution(
    val muscleGroup: String,
    val percentage: Int
)

data class PeakTrainingSlot(
    val timeSlot: String,
    val memberCount: Int
)

data class TrainerAnalyticsUiState(
    val completionRatePercentage: Int = 78,
    val activeAthletesCount: Int = 24,
    val totalAthletesCount: Int = 32,
    val totalStudioVolumeKg: Long = 48200L,
    val averageSessionsPerWeek: Float = 3.4f,
    val muscleDistributions: List<MuscleDistribution> = listOf(
        MuscleDistribution("Chest", 32),
        MuscleDistribution("Back", 26),
        MuscleDistribution("Legs", 20),
        MuscleDistribution("Shoulders", 14),
        MuscleDistribution("Arms", 8)
    ),
    val peakHours: List<PeakTrainingSlot> = listOf(
        PeakTrainingSlot("06:00 - 08:00", 8),
        PeakTrainingSlot("08:00 - 11:00", 4),
        PeakTrainingSlot("16:00 - 19:00", 14),
        PeakTrainingSlot("19:00 - 21:00", 6)
    ),
    val isExportingPdf: Boolean = false,
    val pdfExportComplete: Boolean = false,
    val exportedPdfFileName: String? = null
)

class TrainerAnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerAnalyticsUiState())
    val uiState: StateFlow<TrainerAnalyticsUiState> = _uiState.asStateFlow()

    fun exportPdfReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true, pdfExportComplete = false) }
            delay(1200) // Simulate PDF generation
            _uiState.update {
                it.copy(
                    isExportingPdf = false,
                    pdfExportComplete = true,
                    exportedPdfFileName = "GYMFLOW_Studio_Analytics_Nov2026.pdf"
                )
            }
        }
    }

    fun dismissPdfNotification() {
        _uiState.update { it.copy(pdfExportComplete = false) }
    }
}
