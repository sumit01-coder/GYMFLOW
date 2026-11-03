package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gymflow.domain.model.SetLog

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SetLogEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val actualReps: Int,
    val actualWeightKg: Float,
    val completedAtEpochMs: Long,
    val isSynced: Boolean = false
) {
    fun toDomain(): SetLog = SetLog(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        setNumber = setNumber,
        targetReps = targetReps,
        targetWeightKg = targetWeightKg,
        actualReps = actualReps,
        actualWeightKg = actualWeightKg,
        completedAtEpochMs = completedAtEpochMs
    )

    companion object {
        fun fromDomain(log: SetLog, isSynced: Boolean = false): SetLogEntity = SetLogEntity(
            id = log.id,
            sessionId = log.sessionId,
            exerciseId = log.exerciseId,
            exerciseName = log.exerciseName,
            setNumber = log.setNumber,
            targetReps = log.targetReps,
            targetWeightKg = log.targetWeightKg,
            actualReps = log.actualReps,
            actualWeightKg = log.actualWeightKg,
            completedAtEpochMs = log.completedAtEpochMs,
            isSynced = isSynced
        )
    }
}
