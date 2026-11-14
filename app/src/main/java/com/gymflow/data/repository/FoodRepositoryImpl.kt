package com.gymflow.data.repository

import android.graphics.Bitmap
import com.gymflow.core.ai.GeminiFoodVisionService
import com.gymflow.core.database.FoodDao
import com.gymflow.core.database.FoodLogEntity
import com.gymflow.core.network.OpenFoodFactsClient
import com.gymflow.core.network.OpenFoodProduct
import com.gymflow.domain.model.AiFoodVisionResult
import com.gymflow.domain.repository.DailyMacros
import com.gymflow.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    private val openFoodFactsClient: OpenFoodFactsClient,
    private val geminiFoodVisionService: GeminiFoodVisionService
) : FoodRepository {

    override fun getFoodLogsForDay(
        memberId: String,
        startMs: Long,
        endMs: Long
    ): Flow<List<FoodLogEntity>> = foodDao.getFoodLogsForDay(memberId, startMs, endMs)

    override fun getTodayCalories(
        memberId: String,
        startOfDayMs: Long
    ): Flow<Int> = foodDao.getTodayCaloriesTotal(memberId, startOfDayMs).map { it ?: 0 }

    override fun getTodayMacros(
        memberId: String,
        startOfDayMs: Long
    ): Flow<DailyMacros> {
        val caloriesFlow = foodDao.getTodayCaloriesTotal(memberId, startOfDayMs)
        val proteinFlow = foodDao.getTodayProteinTotal(memberId, startOfDayMs)
        val carbsFlow = foodDao.getTodayCarbsTotal(memberId, startOfDayMs)
        val fatFlow = foodDao.getTodayFatTotal(memberId, startOfDayMs)

        return combine(caloriesFlow, proteinFlow, carbsFlow, fatFlow) { cal, p, c, f ->
            DailyMacros(
                totalCaloriesKcal = cal ?: 0,
                totalProteinGrams = p ?: 0f,
                totalCarbsGrams = c ?: 0f,
                totalFatGrams = f ?: 0f
            )
        }
    }

    override suspend fun logFood(foodLog: FoodLogEntity) {
        foodDao.upsertFoodLog(foodLog)
    }

    override suspend fun deleteFoodLog(id: String) {
        foodDao.deleteFoodLog(id)
    }

    override suspend fun scanBarcode(barcode: String): Result<OpenFoodProduct> {
        return openFoodFactsClient.getProductByBarcode(barcode)
    }

    override suspend fun analyzeFoodImage(bitmap: Bitmap): Result<AiFoodVisionResult> {
        return geminiFoodVisionService.analyzeFoodImage(bitmap)
    }
}
