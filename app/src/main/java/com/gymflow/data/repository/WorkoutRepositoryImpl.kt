package com.gymflow.data.repository

import com.gymflow.core.common.Resource
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.network.GymFlowApiClient
import com.gymflow.data.sync.SyncManager
import com.gymflow.data.sync.SyncWorker
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import com.gymflow.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiClient: GymFlowApiClient,
    private val syncManager: SyncManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : WorkoutRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun getActivePlan(memberId: String): Flow<WorkoutPlan?> {
        return planDao.getActivePlanWithDays(memberId).map { it?.toDomain() }
    }

    override fun getPlanById(planId: String): Flow<WorkoutPlan?> {
        return planDao.getPlanWithDays(planId).map { it?.toDomain() }
    }

    override fun savePlan(plan: WorkoutPlan): Flow<Resource<WorkoutPlan>> = flow {
        emit(Resource.Loading)
        try {
            // Local persistence first (offline-first)
            planDao.replaceCompletePlan(plan)

            // Direct Firestore synchronization
            try {
                firestoreSyncManager.saveWorkoutPlan(plan)
            } catch (_: Exception) {
                // Offline fallback - queue below handles eventual consistency
            }

            // Queue for remote synchronization
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_PLAN,
                    entityId = plan.id,
                    payloadJson = json.encodeToString(plan),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(plan))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to save plan locally: ${e.localizedMessage}", e))
        }
    }

    override fun refreshPlanFromServer(memberId: String): Flow<Resource<WorkoutPlan>> = flow {
        emit(Resource.Loading)
        try {
            firestoreSyncManager.pullAllUserDataFromFirestore(memberId)
            val updatedPlan = planDao.getActivePlanWithDaysSync(memberId)?.toDomain()
            if (updatedPlan != null) {
                emit(Resource.Success(updatedPlan))
                return@flow
            }
        } catch (_: Exception) {}

        // Fallback to API client
        val response = apiClient.getActivePlan(memberId)
        if (response.success && response.data != null) {
            val remotePlan = response.data
            planDao.replaceCompletePlan(remotePlan)
            emit(Resource.Success(remotePlan))
        } else {
            emit(Resource.Error(response.message ?: "Failed to refresh plan"))
        }
    }

    override fun startWorkoutSession(
        memberId: String,
        planId: String?,
        workoutTitle: String,
        totalSets: Int
    ): Flow<Resource<WorkoutSession>> = flow {
        emit(Resource.Loading)
        try {
            val sessionId = UUID.randomUUID().toString()
            val session = WorkoutSession(
                id = sessionId,
                memberId = memberId,
                planId = planId,
                workoutTitle = workoutTitle,
                startTimeEpochMs = System.currentTimeMillis(),
                endTimeEpochMs = null,
                durationSeconds = 0,
                totalVolumeKg = 0f,
                caloriesBurned = 0,
                isCompleted = false,
                setsCompleted = 0,
                totalSets = totalSets
            )

            workoutSessionDao.upsertSession(WorkoutSessionEntity.fromDomain(session, isSynced = false))
            emit(Resource.Success(session))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to start session: ${e.localizedMessage}", e))
        }
    }

    override fun recordSet(setLog: SetLog): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            // Upsert set log locally
            workoutSessionDao.upsertSetLog(SetLogEntity.fromDomain(setLog, isSynced = false))

            // Update session set completion count and volume
            val currentSession = workoutSessionDao.getSessionById(setLog.sessionId)
            if (currentSession != null) {
                val updatedSetsCompleted = currentSession.setsCompleted + 1
                val addedVolume = setLog.actualWeightKg * setLog.actualReps
                val updatedSession = currentSession.copy(
                    setsCompleted = updatedSetsCompleted,
                    totalVolumeKg = currentSession.totalVolumeKg + addedVolume
                )
                workoutSessionDao.upsertSession(updatedSession)
            }

            // Queue for sync
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_SET_LOG,
                    entityId = setLog.id,
                    payloadJson = json.encodeToString(setLog),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to record set: ${e.localizedMessage}", e))
        }
    }

    override fun completeWorkoutSession(
        sessionId: String,
        durationSeconds: Long,
        totalVolumeKg: Float,
        caloriesBurned: Int
    ): Flow<Resource<WorkoutSession>> = flow {
        emit(Resource.Loading)
        try {
            val existing = workoutSessionDao.getSessionById(sessionId)
            if (existing != null) {
                val completedSession = existing.copy(
                    isCompleted = true,
                    endTimeEpochMs = System.currentTimeMillis(),
                    durationSeconds = durationSeconds,
                    totalVolumeKg = totalVolumeKg,
                    caloriesBurned = caloriesBurned,
                    isSynced = false
                )
                workoutSessionDao.upsertSession(completedSession)

                // Direct connection-oriented push to Cloud Firestore
                val setLogs = workoutSessionDao.getSetLogsSync(sessionId)
                try {
                    firestoreSyncManager.saveWorkoutSessionWithSets(completedSession, setLogs)
                    workoutSessionDao.markSessionSynced(sessionId, true)
                    setLogs.forEach { log -> workoutSessionDao.markSetLogSynced(log.id, true) }
                } catch (_: Exception) {}

                val domainSession = completedSession.toDomain()

                // Queue entire session for sync
                syncQueueDao.enqueue(
                    SyncQueueEntity(
                        entityType = SyncWorker.ENTITY_TYPE_SESSION,
                        entityId = sessionId,
                        payloadJson = json.encodeToString(domainSession),
                        status = SyncStatus.PENDING,
                        timestamp = System.currentTimeMillis()
                    )
                )

                syncManager.triggerOneTimeSync()
                emit(Resource.Success(domainSession))
            } else {
                emit(Resource.Error("Session not found: $sessionId"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to complete workout session: ${e.localizedMessage}", e))
        }
    }

    override fun getActiveSession(memberId: String): Flow<WorkoutSession?> = flow {
        val active = workoutSessionDao.getActiveSession(memberId)?.toDomain()
        emit(active)
    }

    override fun getRecentSessions(memberId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getRecentSessions(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionWithSets(sessionId: String): Flow<Pair<WorkoutSession, List<SetLog>>?> {
        return workoutSessionDao.getSessionWithSets(sessionId).map { wrapper ->
            wrapper?.let {
                it.session.toDomain() to it.setLogs.map { log -> log.toDomain() }
            }
        }
    }

    override fun refreshSessionsFromServer(memberId: String): Flow<Resource<List<WorkoutSession>>> = flow {
        emit(Resource.Loading)
        try {
            firestoreSyncManager.pullAllUserDataFromFirestore(memberId)
        } catch (_: Exception) {}

        val response = apiClient.getRecentSessions(memberId)
        if (response.success && response.data != null) {
            val sessions = response.data
            sessions.forEach { s ->
                workoutSessionDao.upsertSession(WorkoutSessionEntity.fromDomain(s, isSynced = true))
            }
            emit(Resource.Success(sessions))
        } else {
            val localSessions = workoutSessionDao.getRecentSessionsSync(memberId).map { it.toDomain() }
            if (localSessions.isNotEmpty()) {
                emit(Resource.Success(localSessions))
            } else {
                emit(Resource.Error(response.message ?: "Failed to refresh sessions"))
            }
        }
    }
}
