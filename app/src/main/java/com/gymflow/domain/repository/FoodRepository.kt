package com.gymflow.domain.repository

import android.graphics.Bitmap
import com.gymflow.core.database.FoodLogEntity
import com.gymflow.core.network.OpenFoodProduct
import com.gymflow.domain.model.AiFoodVisionResult
import kotlinx.coroutines.flow.Flow

data class DailyMacros(
    val totalCaloriesKcal: Int = 0,
    val totalProteinGrams: Float = 0f,
    val totalCarbsGrams: Float = 0f,
    val totalFatGrams: Float = 0f,
    val targetCaloriesKcal: Int = 2200,
    val targetProteinGrams: Float = 150f,
    val targetCarbsGrams: Float = 220f,
    val targetFatGrams: Float = 65f
)

interface FoodRepository {
    fun getFoodLogsForDay(memberId: String, startMs: Long, endMs: Long): Flow<List<FoodLogEntity>>
    fun getTodayCalories(memberId: String, startOfDayMs: Long): Flow<Int>
    fun getTodayMacros(memberId: String, startOfDayMs: Long): Flow<DailyMacros>
    suspend fun logFood(foodLog: FoodLogEntity)
    suspend fun deleteFoodLog(id: String)
    suspend fun scanBarcode(barcode: String): Result<OpenFoodProduct>
    suspend fun analyzeFoodImage(bitmap: Bitmap): Result<AiFoodVisionResult>
}
