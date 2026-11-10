package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    // Weight Logs
    @Upsert
    suspend fun upsertWeightLog(log: WeightLogEntity)

    @Query("SELECT * FROM weight_logs WHERE memberId = :memberId ORDER BY recordedAtEpochMs DESC")
    fun getWeightHistory(memberId: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE memberId = :memberId ORDER BY recordedAtEpochMs DESC LIMIT 1")
    fun getLatestWeightLog(memberId: String): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY recordedAtEpochMs DESC LIMIT 1")
    fun getLatestAnyWeightLog(): Flow<WeightLogEntity?>

    @Query("SELECT * FROM weight_logs ORDER BY recordedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestAnyWeightLogSync(): WeightLogEntity?

    @Query("SELECT * FROM weight_logs WHERE id = :id LIMIT 1")
    suspend fun getWeightById(id: String): WeightLogEntity?

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLog(id: String)

    @Query("UPDATE weight_logs SET isSynced = :synced WHERE id = :id")
    suspend fun markWeightLogSynced(id: String, synced: Boolean = true)

    @Query("SELECT * FROM weight_logs WHERE isSynced = 0")
    suspend fun getUnsyncedWeightLogs(): List<WeightLogEntity>

    // Hydration Logs
    @Upsert
    suspend fun upsertHydrationLog(log: HydrationLogEntity)

    @Query("SELECT * FROM hydration_logs WHERE memberId = :memberId AND recordedAtEpochMs BETWEEN :startMs AND :endMs ORDER BY recordedAtEpochMs ASC")
    fun getHydrationLogsForRange(memberId: String, startMs: Long, endMs: Long): Flow<List<HydrationLogEntity>>

    @Query("SELECT SUM(amountMl) FROM hydration_logs WHERE (memberId = :memberId OR memberId = 'default_member' OR :memberId = 'default_member') AND recordedAtEpochMs >= :startOfDayMs")
    fun getTodayHydrationTotal(memberId: String, startOfDayMs: Long): Flow<Int?>

    @Query("SELECT SUM(amountMl) FROM hydration_logs WHERE (memberId = :memberId OR memberId = 'default_member' OR :memberId = 'default_member') AND recordedAtEpochMs >= :startOfDayMs")
    suspend fun getTodayHydrationTotalSync(memberId: String, startOfDayMs: Long): Int?

    @Query("SELECT * FROM hydration_logs WHERE (memberId = :memberId OR memberId = 'default_member' OR :memberId = 'default_member') AND recordedAtEpochMs >= :startOfDayMs ORDER BY recordedAtEpochMs DESC")
    fun getTodayHydrationLogs(memberId: String, startOfDayMs: Long): Flow<List<HydrationLogEntity>>

    @Query("SELECT * FROM hydration_logs WHERE (memberId = :memberId OR memberId = 'default_member') ORDER BY recordedAtEpochMs DESC")
    fun getAllHydrationLogs(memberId: String): Flow<List<HydrationLogEntity>>

    @Query("DELETE FROM hydration_logs WHERE id = :id")
    suspend fun deleteHydrationLog(id: String)

    @Query("DELETE FROM hydration_logs WHERE memberId = :memberId AND recordedAtEpochMs >= :startOfDayMs")
    suspend fun clearTodayHydrationLogs(memberId: String, startOfDayMs: Long)

    @Query("UPDATE hydration_logs SET isSynced = :synced WHERE id = :id")
    suspend fun markHydrationLogSynced(id: String, synced: Boolean = true)

    @Query("SELECT * FROM hydration_logs WHERE isSynced = 0")
    suspend fun getUnsyncedHydrationLogs(): List<HydrationLogEntity>

    // Body Measurements
    @Upsert
    suspend fun upsertMeasurement(measurement: BodyMeasurementEntity)

    @Query("SELECT * FROM body_measurements WHERE memberId = :memberId ORDER BY recordedAtEpochMs DESC")
    fun getMeasurements(memberId: String): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements WHERE memberId = :memberId ORDER BY recordedAtEpochMs DESC LIMIT 1")
    fun getLatestMeasurement(memberId: String): Flow<BodyMeasurementEntity?>

    @Query("UPDATE body_measurements SET isSynced = :synced WHERE id = :id")
    suspend fun markMeasurementSynced(id: String, synced: Boolean = true)

    @Query("SELECT * FROM body_measurements WHERE isSynced = 0")
    suspend fun getUnsyncedMeasurements(): List<BodyMeasurementEntity>

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteMeasurement(id: String)
}
