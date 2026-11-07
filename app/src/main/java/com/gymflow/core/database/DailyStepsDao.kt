package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepsDao {

    @Query("SELECT * FROM daily_steps WHERE memberId = :memberId AND date = :date LIMIT 1")
    fun getDailyStepsFlow(memberId: String, date: String): Flow<DailyStepsEntity?>

    @Query("SELECT * FROM daily_steps WHERE memberId = :memberId AND date = :date LIMIT 1")
    suspend fun getDailyStepsSync(memberId: String, date: String): DailyStepsEntity?

    @Query("SELECT * FROM daily_steps WHERE memberId = :memberId ORDER BY date DESC LIMIT :limit")
    fun getRecentDailySteps(memberId: String, limit: Int = 14): Flow<List<DailyStepsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailySteps(steps: DailyStepsEntity)

    @Query("SELECT * FROM daily_steps WHERE isSynced = 0")
    suspend fun getUnsyncedSteps(): List<DailyStepsEntity>

    @Query("UPDATE daily_steps SET isSynced = :synced WHERE id = :id")
    suspend fun markStepsSynced(id: String, synced: Boolean)

    @Query("UPDATE daily_steps SET targetGoal = :goal WHERE memberId = :memberId AND date = :date")
    suspend fun updateTargetGoal(memberId: String, date: String, goal: Int)

    @Query("DELETE FROM daily_steps WHERE id = :id")
    suspend fun deleteDailySteps(id: String)
}

