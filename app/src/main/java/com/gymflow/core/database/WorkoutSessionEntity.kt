package com.gymflow.core.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.gymflow.domain.model.WorkoutSession

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val planId: String? = null,
    val workoutTitle: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val durationSeconds: Long = 0,
    val totalVolumeKg: Float = 0f,
    val caloriesBurned: Int = 0,
    val isCompleted: Boolean = false,
    val setsCompleted: Int = 0,
    val totalSets: Int = 0,
    val isSynced: Boolean = false
) {
    fun toDomain(): WorkoutSession = WorkoutSession(
        id = id,
        memberId = memberId,
        planId = planId,
        workoutTitle = workoutTitle,
        startTimeEpochMs = startTimeEpochMs,
        endTimeEpochMs = endTimeEpochMs,
        durationSeconds = durationSeconds,
        totalVolumeKg = totalVolumeKg,
        caloriesBurned = caloriesBurned,
        isCompleted = isCompleted,
        setsCompleted = setsCompleted,
        totalSets = totalSets
    )

    companion object {
        fun fromDomain(session: WorkoutSession, isSynced: Boolean = false): WorkoutSessionEntity =
            WorkoutSessionEntity(
                id = session.id,
                memberId = session.memberId,
                planId = session.planId,
                workoutTitle = session.workoutTitle,
                startTimeEpochMs = session.startTimeEpochMs,
                endTimeEpochMs = session.endTimeEpochMs,
                durationSeconds = session.durationSeconds,
                totalVolumeKg = session.totalVolumeKg,
                caloriesBurned = session.caloriesBurned,
                isCompleted = session.isCompleted,
                setsCompleted = session.setsCompleted,
                totalSets = session.totalSets,
                isSynced = isSynced
            )
    }
}

data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val setLogs: List<SetLogEntity>
)
