package com.gymflow.domain.repository

import com.gymflow.core.common.Resource
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.WeightLog
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun logWeight(
        memberId: String,
        preWeightKg: Float,
        postWeightKg: Float,
        notes: String = ""
    ): Flow<Resource<WeightLog>>

    fun getWeightHistory(memberId: String): Flow<List<WeightLog>>

    fun getLatestWeight(memberId: String): Flow<WeightLog?>

    fun deleteWeightLog(id: String): Flow<Resource<Unit>>

    fun logHydration(
        memberId: String,
        amountMl: Int,
        dailyGoalMl: Int = 3000
    ): Flow<Resource<HydrationLog>>

    fun getTodayHydration(memberId: String): Flow<Int>

    fun logBodyMeasurement(measurement: BodyMeasurement): Flow<Resource<BodyMeasurement>>

    fun getLatestMeasurement(memberId: String): Flow<BodyMeasurement?>

    fun getMeasurementHistory(memberId: String): Flow<List<BodyMeasurement>>
}
