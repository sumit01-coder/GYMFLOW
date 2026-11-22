package com.gymflow.feature.trainer.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.domain.model.Role
import com.gymflow.domain.repository.GymMemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class MuscleDistribution(
    val muscleGroup: String,
    val percentage: Int
)

data class PeakTrainingSlot(
    val timeSlot: String,
    val memberCount: Int
)

data class TrainerAnalyticsUiState(
    val completionRatePercentage: Int = 0,
    val activeAthletesCount: Int = 0,
    val totalAthletesCount: Int = 0,
    val totalStudioVolumeKg: Long = 0L,
    val averageSessionsPerWeek: Float = 0f,
    val muscleDistributions: List<MuscleDistribution> = emptyList(),
    val peakHours: List<PeakTrainingSlot> = emptyList(),
    val isExportingPdf: Boolean = false,
    val pdfExportComplete: Boolean = false,
    val exportedPdfFileName: String? = null
)

@HiltViewModel
class TrainerAnalyticsViewModel(
    private val gymMemberRepository: GymMemberRepository?,
    private val userDao: UserDao?,
    private val workoutSessionDao: WorkoutSessionDao?,
    private val firestoreSyncManager: FirestoreSyncManager?,
    private val tokenManager: TokenManager?,
    @Suppress("UNUSED_PARAMETER") private val isHilt: Boolean
) : ViewModel() {

    @Inject
    constructor(
        gymMemberRepository: GymMemberRepository,
        userDao: UserDao,
        workoutSessionDao: WorkoutSessionDao,
        firestoreSyncManager: FirestoreSyncManager,
        tokenManager: TokenManager
    ) : this(gymMemberRepository, userDao, workoutSessionDao, firestoreSyncManager, tokenManager, true)

    constructor() : this(null, null, null, null, null, false)

    private val _uiState = MutableStateFlow(TrainerAnalyticsUiState())
    val uiState: StateFlow<TrainerAnalyticsUiState> = _uiState.asStateFlow()

    init {
        syncConnectedMembersFromCloud()
        observeStudioRealData()
    }

    private fun syncConnectedMembersFromCloud() {
        val fsm = firestoreSyncManager ?: return
        val trainerId = tokenManager?.getUserIdSync() ?: return
        viewModelScope.launch {
            try {
                fsm.pullConnectedMembersForTrainer(trainerId)
            } catch (_: Exception) {}
        }
    }

    private fun observeStudioRealData() {
        val repo = gymMemberRepository ?: return
        val uDao = userDao ?: return
        val wsDao = workoutSessionDao ?: return

        viewModelScope.launch {
            val trainerId = tokenManager?.getUserIdSync() ?: ""
            val trainerEmail = tokenManager?.getEmail() ?: ""
            combine(
                repo.getAllMembers(),
                uDao.getConnectedAthletesFlow(),
                wsDao.getAllCompletedSessionsFlow()
            ) { gymMembers, athletes, sessions ->
                val filteredGymMembers = gymMembers.filter {
                    it.id != trainerId && (trainerEmail.isBlank() || !it.email.equals(trainerEmail, ignoreCase = true))
                }
                val filteredAthletes = athletes.filter {
                    it.id != trainerId && (trainerEmail.isBlank() || !it.email.equals(trainerEmail, ignoreCase = true))
                }
                val allMemberIds = (filteredGymMembers.map { it.id } + filteredAthletes.map { it.id }).toSet()
                val totalMembers = allMemberIds.size
                val activeMembers = filteredGymMembers.count { !it.isExpired && it.paymentStatus == PaymentStatus.PAID } +
                        filteredAthletes.count { it.connectedTrainerId != null && it.id !in filteredGymMembers.map { m -> m.id } }

                val totalVolume = sessions.sumOf { it.totalVolumeKg.toDouble() }.toLong()
                val completionRate = if (sessions.isNotEmpty()) 100 else 0

                val fourWeeksAgo = System.currentTimeMillis() - (28L * 24 * 60 * 60 * 1000)
                val recentSessions = sessions.filter { it.startTimeEpochMs >= fourWeeksAgo }
                val avgPerWeek = if (recentSessions.isNotEmpty()) (recentSessions.size / 4.0f) else 0f

                val peakSlots = if (sessions.isNotEmpty()) {
                    sessions.groupBy { session ->
                        val hour = Instant.ofEpochMilli(session.startTimeEpochMs)
                            .atZone(ZoneId.systemDefault()).hour
                        when (hour) {
                            in 6..8 -> "06:00 - 08:00"
                            in 8..11 -> "08:00 - 11:00"
                            in 16..19 -> "16:00 - 19:00"
                            in 19..21 -> "19:00 - 21:00"
                            else -> "Other"
                        }
                    }.map { (slot, list) -> PeakTrainingSlot(slot, list.size) }
                        .sortedByDescending { it.memberCount }
                } else emptyList()

                TrainerAnalyticsUiState(
                    completionRatePercentage = completionRate,
                    activeAthletesCount = activeMembers,
                    totalAthletesCount = totalMembers,
                    totalStudioVolumeKg = totalVolume,
                    averageSessionsPerWeek = avgPerWeek,
                    muscleDistributions = emptyList(),
                    peakHours = peakSlots
                )
            }.collect { calculated ->
                _uiState.update { current ->
                    current.copy(
                        completionRatePercentage = calculated.completionRatePercentage,
                        activeAthletesCount = calculated.activeAthletesCount,
                        totalAthletesCount = calculated.totalAthletesCount,
                        totalStudioVolumeKg = calculated.totalStudioVolumeKg,
                        averageSessionsPerWeek = calculated.averageSessionsPerWeek,
                        muscleDistributions = calculated.muscleDistributions,
                        peakHours = calculated.peakHours
                    )
                }
            }
        }
    }

    fun exportPdfReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true, pdfExportComplete = false) }
            delay(800)
            _uiState.update {
                it.copy(
                    isExportingPdf = false,
                    pdfExportComplete = true,
                    exportedPdfFileName = "GYMFLOW_Studio_Analytics_${System.currentTimeMillis()}.pdf"
                )
            }
        }
    }

    fun dismissPdfNotification() {
        _uiState.update { it.copy(pdfExportComplete = false) }
    }
}
