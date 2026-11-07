package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutdoorWorkoutDao {

    @Query("SELECT * FROM outdoor_workouts WHERE memberId = :memberId ORDER BY startTimeEpochMs DESC")
    fun getOutdoorWorkouts(memberId: String): Flow<List<OutdoorWorkoutEntity>>

    @Query("SELECT * FROM outdoor_workouts WHERE id = :id LIMIT 1")
    suspend fun getOutdoorWorkoutById(id: String): OutdoorWorkoutEntity?

    @Query("SELECT * FROM outdoor_workouts WHERE memberId = :memberId AND isCompleted = 0 ORDER BY startTimeEpochMs DESC LIMIT 1")
    suspend fun getActiveOutdoorWorkout(memberId: String): OutdoorWorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutdoorWorkout(workout: OutdoorWorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsPoint(point: OutdoorGpsPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsPoints(points: List<OutdoorGpsPointEntity>)

    @Query("SELECT * FROM outdoor_gps_points WHERE workoutId = :workoutId ORDER BY timestampEpochMs ASC")
    suspend fun getGpsPointsForWorkout(workoutId: String): List<OutdoorGpsPointEntity>

    @Query("SELECT * FROM outdoor_gps_points WHERE workoutId = :workoutId ORDER BY timestampEpochMs ASC")
    fun observeGpsPointsForWorkout(workoutId: String): Flow<List<OutdoorGpsPointEntity>>

    @Query("DELETE FROM outdoor_workouts WHERE id = :id")
    suspend fun deleteOutdoorWorkout(id: String)

    @Query("DELETE FROM outdoor_gps_points WHERE workoutId = :workoutId")
    suspend fun deleteGpsPointsForWorkout(workoutId: String)

    @Query("SELECT * FROM outdoor_workouts WHERE isSynced = 0 AND isCompleted = 1")
    suspend fun getUnsyncedWorkouts(): List<OutdoorWorkoutEntity>

    @Query("UPDATE outdoor_workouts SET isSynced = :synced WHERE id = :id")
    suspend fun markWorkoutSynced(id: String, synced: Boolean)
}

