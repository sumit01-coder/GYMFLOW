package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.WeightLog

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val preWeightKg: Float,
    val postWeightKg: Float,
    val deltaKg: Float = postWeightKg - preWeightKg,
    val recordedAtEpochMs: Long,
    val notes: String = "",
    val isSynced: Boolean = false
) {
    fun toDomain(): WeightLog = WeightLog(
        id = id,
        memberId = memberId,
        preWeightKg = preWeightKg,
        postWeightKg = postWeightKg,
        deltaKg = deltaKg,
        recordedAtEpochMs = recordedAtEpochMs,
        notes = notes
    )

    companion object {
        fun fromDomain(log: WeightLog, isSynced: Boolean = false): WeightLogEntity = WeightLogEntity(
            id = log.id,
            memberId = log.memberId,
            preWeightKg = log.preWeightKg,
            postWeightKg = log.postWeightKg,
            deltaKg = log.deltaKg,
            recordedAtEpochMs = log.recordedAtEpochMs,
            notes = log.notes,
            isSynced = isSynced
        )
    }
}
