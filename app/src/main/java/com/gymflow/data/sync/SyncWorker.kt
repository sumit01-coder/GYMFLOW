package com.gymflow.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.network.GymFlowApiClient
import com.gymflow.core.network.SyncSessionRequest
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun syncQueueDao(): SyncQueueDao
        fun workoutSessionDao(): WorkoutSessionDao
        fun weightDao(): WeightDao
        fun planDao(): PlanDao
        fun apiClient(): GymFlowApiClient
        fun firestoreSyncManager(): FirestoreSyncManager
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val tag = "SyncWorker"

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        )

        val syncQueueDao = entryPoint.syncQueueDao()
        val workoutSessionDao = entryPoint.workoutSessionDao()
        val weightDao = entryPoint.weightDao()
        val apiClient = entryPoint.apiClient()
        val firestoreSyncManager = entryPoint.firestoreSyncManager()

        val pending = syncQueueDao.getPendingItems(limit = 25)
        if (pending.isEmpty()) {
            Log.d(tag, "No pending items in sync queue.")
            return Result.success()
        }

        Log.d(tag, "Found ${pending.size} pending items in sync queue.")
        var hasFailures = false

        for (item in pending) {
            try {
                syncQueueDao.updateStatus(
                    id = item.id,
                    status = SyncStatus.IN_PROGRESS,
                    retryCount = item.retryCount
                )

                val success = processItem(item, apiClient, workoutSessionDao, weightDao, firestoreSyncManager)
                if (success) {
                    syncQueueDao.delete(item.id)
                    Log.d(tag, "Synced entity ${item.entityType}:${item.entityId} successfully.")
                } else {
                    hasFailures = true
                    handleFailure(syncQueueDao, item, "Sync failed for entity")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed syncing item ${item.id}", e)
                hasFailures = true
                handleFailure(syncQueueDao, item, e.message ?: "Unknown error")
            }
        }

        return if (hasFailures) {
            if (runAttemptCount < 3) Result.retry() else Result.success()
        } else {
            Result.success()
        }
    }

    private suspend fun processItem(
        item: SyncQueueEntity,
        apiClient: GymFlowApiClient,
        workoutSessionDao: WorkoutSessionDao,
        weightDao: WeightDao,
        firestoreSyncManager: FirestoreSyncManager
    ): Boolean {
        var firestoreSynced = false
        try {
            when (item.entityType) {
                ENTITY_TYPE_SESSION -> {
                    val session = json.decodeFromString<WorkoutSession>(item.payloadJson)
                    val sessionEntity = WorkoutSessionEntity.fromDomain(session)
                    val setLogs = workoutSessionDao.getSetLogsSync(session.id)
                    firestoreSyncManager.saveWorkoutSessionWithSets(sessionEntity, setLogs)
                    workoutSessionDao.markSessionSynced(session.id, true)
                    setLogs.forEach { log -> workoutSessionDao.markSetLogSynced(log.id, true) }
                    firestoreSynced = true
                }
                ENTITY_TYPE_SET_LOG -> {
                    val setLog = json.decodeFromString<SetLog>(item.payloadJson)
                    workoutSessionDao.markSetLogSynced(setLog.id, true)
                    firestoreSynced = true
                }
                ENTITY_TYPE_WEIGHT -> {
                    val weightLog = json.decodeFromString<WeightLog>(item.payloadJson)
                    val weightEntity = WeightLogEntity.fromDomain(weightLog)
                    firestoreSyncManager.saveWeightLog(weightEntity)
                    weightDao.markWeightLogSynced(weightLog.id, true)
                    firestoreSynced = true
                }
                ENTITY_TYPE_HYDRATION -> {
                    val hydrationLog = json.decodeFromString<HydrationLog>(item.payloadJson)
                    val hydrationEntity = HydrationLogEntity.fromDomain(hydrationLog)
                    firestoreSyncManager.saveHydrationLog(hydrationEntity)
                    weightDao.markHydrationLogSynced(hydrationLog.id, true)
                    firestoreSynced = true
                }
                ENTITY_TYPE_MEASUREMENT -> {
                    val measurement = json.decodeFromString<BodyMeasurement>(item.payloadJson)
                    val measurementEntity = BodyMeasurementEntity.fromDomain(measurement)
                    firestoreSyncManager.saveBodyMeasurement(measurementEntity)
                    weightDao.markMeasurementSynced(measurement.id, true)
                    firestoreSynced = true
                }
                ENTITY_TYPE_PLAN -> {
                    val plan = json.decodeFromString<WorkoutPlan>(item.payloadJson)
                    firestoreSyncManager.saveWorkoutPlan(plan)
                    firestoreSynced = true
                }
                else -> {
                    firestoreSynced = true
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Firestore sync failed for ${item.entityType}:${item.entityId}", e)
        }

        // Secondary HTTP API sync (non-blocking fallback)
        try {
            when (item.entityType) {
                ENTITY_TYPE_SESSION -> {
                    val session = json.decodeFromString<WorkoutSession>(item.payloadJson)
                    val setLogs = workoutSessionDao.getSetLogsSync(session.id).map { it.toDomain() }
                    apiClient.syncWorkoutSession(SyncSessionRequest(session, setLogs))
                }
                ENTITY_TYPE_WEIGHT -> {
                    val weightLog = json.decodeFromString<WeightLog>(item.payloadJson)
                    apiClient.logWeight(weightLog)
                }
                ENTITY_TYPE_HYDRATION -> {
                    val hydrationLog = json.decodeFromString<HydrationLog>(item.payloadJson)
                    apiClient.logHydration(hydrationLog)
                }
                ENTITY_TYPE_MEASUREMENT -> {
                    val measurement = json.decodeFromString<BodyMeasurement>(item.payloadJson)
                    apiClient.logBodyMeasurement(measurement)
                }
                ENTITY_TYPE_PLAN -> {
                    val plan = json.decodeFromString<WorkoutPlan>(item.payloadJson)
                    apiClient.createOrUpdatePlan(plan)
                }
            }
        } catch (_: Exception) {
            // Secondary network endpoint failure is ignored when Firestore sync completed
        }

        return firestoreSynced
    }

    private suspend fun handleFailure(
        syncQueueDao: SyncQueueDao,
        item: SyncQueueEntity,
        errorMessage: String
    ) {
        val nextRetry = item.retryCount + 1
        if (nextRetry >= MAX_RETRIES) {
            syncQueueDao.updateStatus(
                id = item.id,
                status = SyncStatus.FAILED,
                retryCount = nextRetry,
                errorMessage = errorMessage
            )
        } else {
            syncQueueDao.updateStatus(
                id = item.id,
                status = SyncStatus.PENDING,
                retryCount = nextRetry,
                errorMessage = errorMessage
            )
        }
    }

    companion object {
        const val ENTITY_TYPE_SESSION = "SESSION"
        const val ENTITY_TYPE_SET_LOG = "SET_LOG"
        const val ENTITY_TYPE_WEIGHT = "WEIGHT_LOG"
        const val ENTITY_TYPE_HYDRATION = "HYDRATION"
        const val ENTITY_TYPE_MEASUREMENT = "MEASUREMENT"
        const val ENTITY_TYPE_PLAN = "WORKOUT_PLAN"

        private const val MAX_RETRIES = 5
    }
}
