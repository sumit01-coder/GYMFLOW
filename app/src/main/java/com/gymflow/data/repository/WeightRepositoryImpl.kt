package com.gymflow.data.repository

import com.gymflow.core.common.Resource
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
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
    private val syncManager: SyncManager
) : WeightRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun logWeight(
        memberId: String,
        preWeightKg: Float,
        postWeightKg: Float,
        notes: String
    ): Flow<Resource<WeightLog>> = flow {
        emit(Resource.Loading)
        try {
            val weightLog = WeightLog(
                id = UUID.randomUUID().toString(),
                memberId = memberId,
                preWeightKg = preWeightKg,
                postWeightKg = postWeightKg,
                deltaKg = postWeightKg - preWeightKg,
                recordedAtEpochMs = System.currentTimeMillis(),
                notes = notes
            )

            // Save locally
            weightDao.upsertWeightLog(WeightLogEntity.fromDomain(weightLog, isSynced = false))

            // Queue for sync
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

            weightDao.upsertHydrationLog(HydrationLogEntity.fromDomain(hydrationLog, isSynced = false))

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

    override fun getTodayHydration(memberId: String): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDayMs = calendar.timeInMillis
        return weightDao.getTodayHydrationTotal(memberId, startOfDayMs).map { it ?: 0 }
    }

    override fun logBodyMeasurement(measurement: BodyMeasurement): Flow<Resource<BodyMeasurement>> = flow {
        emit(Resource.Loading)
        try {
            weightDao.upsertMeasurement(BodyMeasurementEntity.fromDomain(measurement, isSynced = false))

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
