package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.HydrationLog

@Entity(tableName = "hydration_logs")
data class HydrationLogEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val amountMl: Int,
    val dailyGoalMl: Int = 3000,
    val recordedAtEpochMs: Long,
    val isSynced: Boolean = false
) {
    fun toDomain(): HydrationLog = HydrationLog(
        id = id,
        memberId = memberId,
        amountMl = amountMl,
        dailyGoalMl = dailyGoalMl,
        recordedAtEpochMs = recordedAtEpochMs
    )

    companion object {
        fun fromDomain(log: HydrationLog, isSynced: Boolean = false): HydrationLogEntity = HydrationLogEntity(
            id = log.id,
            memberId = log.memberId,
            amountMl = log.amountMl,
            dailyGoalMl = log.dailyGoalMl,
            recordedAtEpochMs = log.recordedAtEpochMs,
            isSynced = isSynced
        )
    }
}
