package com.gymflow.data.repository

import com.gymflow.core.common.Resource
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.data.sync.SyncManager
import com.gymflow.data.sync.SyncWorker
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncManager: SyncManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : WeightRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun logWeight(
        memberId: String,
        preWeightKg: Float,
        postWeightKg: Float,
        notes: String,
        existingLogId: String?,
        recordedAtEpochMs: Long?,
        postRecordedAtEpochMs: Long?
    ): Flow<Resource<WeightLog>> = flow {
        emit(Resource.Loading)
        try {
            val now = System.currentTimeMillis()
            val existing = if (existingLogId != null) weightDao.getWeightById(existingLogId) else null

            val weightLog = if (existing != null) {
                val updatedPost = if (postWeightKg > 0f) postWeightKg else existing.postWeightKg
                val pre = existing.preWeightKg
                val delta = if (updatedPost > 0f) updatedPost - pre else 0f
                WeightLog(
                    id = existing.id,
                    memberId = existing.memberId,
                    preWeightKg = pre,
                    postWeightKg = updatedPost,
                    deltaKg = delta,
                    recordedAtEpochMs = existing.recordedAtEpochMs,
                    notes = if (notes.isNotBlank()) notes else existing.notes,
                    postRecordedAtEpochMs = postRecordedAtEpochMs ?: existing.postRecordedAtEpochMs ?: now
                )
            } else {
                WeightLog(
                    id = UUID.randomUUID().toString(),
                    memberId = memberId,
                    preWeightKg = preWeightKg,
                    postWeightKg = postWeightKg,
                    deltaKg = if (postWeightKg > 0f) postWeightKg - preWeightKg else 0f,
                    recordedAtEpochMs = recordedAtEpochMs ?: now,
                    notes = notes,
                    postRecordedAtEpochMs = if (postWeightKg > 0f) (postRecordedAtEpochMs ?: now) else null
                )
            }

            // Save locally
            val weightEntity = WeightLogEntity.fromDomain(weightLog, isSynced = false)
            weightDao.upsertWeightLog(weightEntity)

            // Direct Firebase Firestore sync
            try {
                firestoreSyncManager.saveWeightLog(weightEntity)
                weightDao.markWeightLogSynced(weightEntity.id, true)
            } catch (_: Exception) {}

            // Queue for sync fallback
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_WEIGHT,
                    entityId = weightLog.id,
                    payloadJson = json.encodeToString(weightLog),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(weightLog))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to record weight: ${e.localizedMessage}", e))
        }
    }

    override fun getWeightHistory(memberId: String): Flow<List<WeightLog>> {
        return weightDao.getWeightHistory(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLatestWeight(memberId: String): Flow<WeightLog?> {
        return weightDao.getLatestWeightLog(memberId).map { entity ->
            val resolved = entity ?: weightDao.getLatestAnyWeightLogSync()
            resolved?.toDomain()
        }
    }

    override fun deleteWeightLog(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            weightDao.deleteWeightLog(id)
            try {
                firestoreSyncManager.deleteWeightLog(id)
            } catch (_: Exception) {}
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to delete weight log: ${e.localizedMessage}", e))
        }
    }

    override fun logHydration(
        memberId: String,
        amountMl: Int,
        dailyGoalMl: Int
    ): Flow<Resource<HydrationLog>> = flow {
        emit(Resource.Loading)
        try {
            val hydrationLog = HydrationLog(
                id = UUID.randomUUID().toString(),
                memberId = memberId,
                amountMl = amountMl,
                dailyGoalMl = dailyGoalMl,
                recordedAtEpochMs = System.currentTimeMillis()
            )

            val hydrationEntity = HydrationLogEntity.fromDomain(hydrationLog, isSynced = false)
            weightDao.upsertHydrationLog(hydrationEntity)

            // Direct Firebase Firestore sync
            try {
                firestoreSyncManager.saveHydrationLog(hydrationEntity)
                weightDao.markHydrationLogSynced(hydrationEntity.id, true)
            } catch (_: Exception) {}

            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_HYDRATION,
                    entityId = hydrationLog.id,
                    payloadJson = json.encodeToString(hydrationLog),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(hydrationLog))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to record hydration: ${e.localizedMessage}", e))
        }
    }

    private fun getTodayStartEpochMs(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun getTodayHydration(memberId: String): Flow<Int> {
        val startOfDayMs = getTodayStartEpochMs()
        return weightDao.getTodayHydrationTotal(memberId, startOfDayMs).map { it ?: 0 }
    }

    override fun getTodayHydrationLogs(memberId: String): Flow<List<HydrationLog>> {
        val startOfDayMs = getTodayStartEpochMs()
        return weightDao.getTodayHydrationLogs(memberId, startOfDayMs).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun deleteHydrationLog(id: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            weightDao.deleteHydrationLog(id)
            try {
                firestoreSyncManager.deleteHydrationLog(id)
            } catch (_: Exception) {}
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to delete hydration log: ${e.localizedMessage}", e))
        }
    }

    override fun clearTodayHydration(memberId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val startOfDay = getTodayStartEpochMs()
            weightDao.clearTodayHydrationLogs(memberId, startOfDay)
            try {
                firestoreSyncManager.clearTodayHydrationLogs(memberId, startOfDay)
            } catch (_: Exception) {}
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to clear today's hydration: ${e.localizedMessage}", e))
        }
    }

    override fun logBodyMeasurement(measurement: BodyMeasurement): Flow<Resource<BodyMeasurement>> = flow {
        emit(Resource.Loading)
        try {
            val measurementEntity = BodyMeasurementEntity.fromDomain(measurement, isSynced = false)
            weightDao.upsertMeasurement(measurementEntity)

            // Direct Firebase Firestore sync
            try {
                firestoreSyncManager.saveBodyMeasurement(measurementEntity)
                weightDao.markMeasurementSynced(measurementEntity.id, true)
            } catch (_: Exception) {}

            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_MEASUREMENT,
                    entityId = measurement.id,
                    payloadJson = json.encodeToString(measurement),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(measurement))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to record body measurement: ${e.localizedMessage}", e))
        }
    }

    override fun getLatestMeasurement(memberId: String): Flow<BodyMeasurement?> {
        return weightDao.getLatestMeasurement(memberId).map { it?.toDomain() }
    }

    override fun getMeasurementHistory(memberId: String): Flow<List<BodyMeasurement>> {
        return weightDao.getMeasurements(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
