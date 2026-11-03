package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.BodyMeasurement

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val chestCm: Float,
    val waistCm: Float,
    val armsCm: Float,
    val thighsCm: Float,
    val hipsCm: Float,
    val recordedAtEpochMs: Long,
    val isSynced: Boolean = false
) {
    fun toDomain(): BodyMeasurement = BodyMeasurement(
        id = id,
        memberId = memberId,
        chestCm = chestCm,
        waistCm = waistCm,
        armsCm = armsCm,
        thighsCm = thighsCm,
        hipsCm = hipsCm,
        recordedAtEpochMs = recordedAtEpochMs
    )

    companion object {
        fun fromDomain(measurement: BodyMeasurement, isSynced: Boolean = false): BodyMeasurementEntity =
            BodyMeasurementEntity(
                id = measurement.id,
                memberId = measurement.memberId,
                chestCm = measurement.chestCm,
                waistCm = measurement.waistCm,
                armsCm = measurement.armsCm,
                thighsCm = measurement.thighsCm,
                hipsCm = measurement.hipsCm,
                recordedAtEpochMs = measurement.recordedAtEpochMs,
                isSynced = isSynced
            )
    }
}
