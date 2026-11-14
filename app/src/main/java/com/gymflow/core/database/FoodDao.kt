package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Upsert
    suspend fun upsertFoodLog(log: FoodLogEntity)

    @Query("SELECT * FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member') AND loggedAtEpochMs BETWEEN :startMs AND :endMs ORDER BY loggedAtEpochMs DESC")
    fun getFoodLogsForDay(memberId: String, startMs: Long, endMs: Long): Flow<List<FoodLogEntity>>

    @Query("SELECT SUM(caloriesKcal) FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member' OR :memberId = 'default_member') AND loggedAtEpochMs >= :startOfDayMs")
    fun getTodayCaloriesTotal(memberId: String, startOfDayMs: Long): Flow<Int?>

    @Query("SELECT SUM(caloriesKcal) FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member' OR :memberId = 'default_member') AND loggedAtEpochMs >= :startOfDayMs")
    suspend fun getTodayCaloriesTotalSync(memberId: String, startOfDayMs: Long): Int?

    @Query("SELECT SUM(proteinGrams) FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member') AND loggedAtEpochMs >= :startOfDayMs")
    fun getTodayProteinTotal(memberId: String, startOfDayMs: Long): Flow<Float?>

    @Query("SELECT SUM(carbsGrams) FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member') AND loggedAtEpochMs >= :startOfDayMs")
    fun getTodayCarbsTotal(memberId: String, startOfDayMs: Long): Flow<Float?>

    @Query("SELECT SUM(fatGrams) FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member') AND loggedAtEpochMs >= :startOfDayMs")
    fun getTodayFatTotal(memberId: String, startOfDayMs: Long): Flow<Float?>

    @Query("SELECT * FROM food_logs WHERE (memberId = :memberId OR memberId = 'default_member') ORDER BY loggedAtEpochMs DESC LIMIT 20")
    fun getRecentFoodLogs(memberId: String): Flow<List<FoodLogEntity>>

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLog(id: String)

    @Query("SELECT * FROM food_logs WHERE isSynced = 0")
    suspend fun getUnsyncedFoodLogs(): List<FoodLogEntity>

    @Query("UPDATE food_logs SET isSynced = :synced WHERE id = :id")
    suspend fun markFoodLogSynced(id: String, synced: Boolean = true)
}
