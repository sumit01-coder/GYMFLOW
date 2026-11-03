package com.gymflow.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
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

                val success = processItem(item, apiClient, workoutSessionDao, weightDao)
                if (success) {
                    syncQueueDao.delete(item.id)
                    Log.d(tag, "Synced entity ${item.entityType}:${item.entityId} successfully.")
                } else {
                    hasFailures = true
                    handleFailure(syncQueueDao, item, "API returned error")
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
        weightDao: WeightDao
    ): Boolean {
        return when (item.entityType) {
            ENTITY_TYPE_SESSION -> {
                val session = json.decodeFromString<WorkoutSession>(item.payloadJson)
                val setLogs = workoutSessionDao.getSetLogsSync(session.id).map { it.toDomain() }
                val response = apiClient.syncWorkoutSession(SyncSessionRequest(session, setLogs))
                if (response.success) {
                    workoutSessionDao.markSessionSynced(session.id, true)
                    setLogs.forEach { log -> workoutSessionDao.markSetLogSynced(log.id, true) }
                    true
                } else {
                    false
                }
            }
            ENTITY_TYPE_SET_LOG -> {
                val setLog = json.decodeFromString<SetLog>(item.payloadJson)
                workoutSessionDao.markSetLogSynced(setLog.id, true)
                true
            }
            ENTITY_TYPE_WEIGHT -> {
                val weightLog = json.decodeFromString<WeightLog>(item.payloadJson)
                val response = apiClient.logWeight(weightLog)
                if (response.success) {
                    weightDao.markWeightLogSynced(weightLog.id, true)
                    true
                } else {
                    false
                }
            }
            ENTITY_TYPE_HYDRATION -> {
                val hydrationLog = json.decodeFromString<HydrationLog>(item.payloadJson)
                val response = apiClient.logHydration(hydrationLog)
                if (response.success) {
                    weightDao.markHydrationLogSynced(hydrationLog.id, true)
                    true
                } else {
                    false
                }
            }
            ENTITY_TYPE_MEASUREMENT -> {
                val measurement = json.decodeFromString<BodyMeasurement>(item.payloadJson)
                val response = apiClient.logBodyMeasurement(measurement)
                if (response.success) {
                    weightDao.markMeasurementSynced(measurement.id, true)
                    true
                } else {
                    false
                }
            }
            ENTITY_TYPE_PLAN -> {
                val plan = json.decodeFromString<WorkoutPlan>(item.payloadJson)
                val response = apiClient.createOrUpdatePlan(plan)
                response.success
            }
            else -> {
                Log.w(tag, "Unknown entityType: ${item.entityType}")
                true // Discard unknown type to prevent deadlocks
            }
        }
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
