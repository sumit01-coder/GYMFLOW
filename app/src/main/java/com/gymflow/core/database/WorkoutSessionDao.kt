package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Upsert
    suspend fun upsertSession(session: WorkoutSessionEntity)

    @Upsert
    suspend fun upsertSetLogs(logs: List<SetLogEntity>)

    @Upsert
    suspend fun upsertSetLog(log: SetLogEntity)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): WorkoutSessionEntity?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithSets(sessionId: String): Flow<WorkoutSessionWithSets?>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionWithSetsSync(sessionId: String): WorkoutSessionWithSets?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE memberId = :memberId OR memberId = 'default_member' ORDER BY startTimeEpochMs DESC")
    fun getAllSessionsWithSets(memberId: String): Flow<List<WorkoutSessionWithSets>>

    @Query("SELECT * FROM workout_sessions WHERE memberId = :memberId ORDER BY startTimeEpochMs DESC")
    fun getRecentSessions(memberId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE (memberId = :memberId OR memberId = 'default_member') AND isCompleted = 0 ORDER BY startTimeEpochMs DESC LIMIT 1")
    suspend fun getActiveSession(memberId: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE (memberId = :memberId OR memberId = 'default_member') AND isCompleted = 0 AND startTimeEpochMs >= :startOfDayMs ORDER BY startTimeEpochMs DESC LIMIT 1")
    suspend fun getActiveSessionToday(memberId: String, startOfDayMs: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE (memberId = :memberId OR memberId = 'default_member') AND isCompleted = 0 AND startTimeEpochMs >= :startOfDayMs ORDER BY startTimeEpochMs DESC LIMIT 1")
    fun getActiveSessionTodayFlow(memberId: String, startOfDayMs: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY completedAtEpochMs ASC")
    fun getSetLogs(sessionId: String): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY completedAtEpochMs ASC")
    suspend fun getSetLogsSync(sessionId: String): List<SetLogEntity>

    @Query("""
        SELECT DISTINCT sl.exerciseId FROM set_logs sl
        INNER JOIN workout_sessions ws ON sl.sessionId = ws.id
        WHERE sl.completedAtEpochMs >= :startOfDayMs
        AND (ws.memberId = :memberId OR ws.memberId = 'default_member')
    """)
    fun getCompletedExerciseIdsToday(memberId: String, startOfDayMs: Long): Flow<List<String>>

    @Query("""
        SELECT DISTINCT sl.exerciseId FROM set_logs sl
        INNER JOIN workout_sessions ws ON sl.sessionId = ws.id
        WHERE sl.completedAtEpochMs >= :startOfDayMs
        AND (ws.memberId = :memberId OR ws.memberId = 'default_member')
    """)
    suspend fun getCompletedExerciseIdsTodaySync(memberId: String, startOfDayMs: Long): List<String>

    @Query("UPDATE workout_sessions SET isSynced = :synced WHERE id = :sessionId")
    suspend fun markSessionSynced(sessionId: String, synced: Boolean = true)

    @Query("UPDATE set_logs SET isSynced = :synced WHERE id = :logId")
    suspend fun markSetLogSynced(logId: String, synced: Boolean = true)

    @Query("SELECT * FROM workout_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM set_logs WHERE isSynced = 0")
    suspend fun getUnsyncedSetLogs(): List<SetLogEntity>

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
