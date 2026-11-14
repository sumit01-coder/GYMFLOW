package com.gymflow.feature.member

import com.gymflow.core.database.FoodDao
import com.gymflow.core.database.FoodLogEntity
import com.gymflow.core.network.OpenFoodFactsResponse
import com.gymflow.domain.model.AiFoodVisionResult
import com.gymflow.domain.model.DetectedFoodItem
import com.gymflow.feature.member.fatloss.BodyView
import com.gymflow.feature.member.fatloss.TargetFatLossCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class FakeFoodDao : FoodDao {
    private val logs = mutableMapOf<String, FoodLogEntity>()
    private val flow = MutableStateFlow<List<FoodLogEntity>>(emptyList())

    override suspend fun upsertFoodLog(log: FoodLogEntity) {
        logs[log.id] = log
        flow.value = logs.values.toList()
    }

    override fun getFoodLogsForDay(
        memberId: String,
        startMs: Long,
        endMs: Long
    ): Flow<List<FoodLogEntity>> = flow.map { list ->
        list.filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs in startMs..endMs }
            .sortedByDescending { it.loggedAtEpochMs }
    }

    override fun getTodayCaloriesTotal(memberId: String, startOfDayMs: Long): Flow<Int?> = flow.map { list ->
        val total = list
            .filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }
            .sumOf { it.caloriesKcal }
        if (total == 0 && list.none { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }) null else total
    }

    override suspend fun getTodayCaloriesTotalSync(memberId: String, startOfDayMs: Long): Int? {
        val total = logs.values
            .filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }
            .sumOf { it.caloriesKcal }
        return if (total == 0 && logs.values.none { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }) null else total
    }

    override fun getTodayProteinTotal(memberId: String, startOfDayMs: Long): Flow<Float?> = flow.map { list ->
        list.filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }
            .map { it.proteinGrams }
            .sum()
    }

    override fun getTodayCarbsTotal(memberId: String, startOfDayMs: Long): Flow<Float?> = flow.map { list ->
        list.filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }
            .map { it.carbsGrams }
            .sum()
    }

    override fun getTodayFatTotal(memberId: String, startOfDayMs: Long): Flow<Float?> = flow.map { list ->
        list.filter { (it.memberId == memberId || it.memberId == "default_member") && it.loggedAtEpochMs >= startOfDayMs }
            .map { it.fatGrams }
            .sum()
    }

    override fun getRecentFoodLogs(memberId: String): Flow<List<FoodLogEntity>> = flow.map { list ->
        list.filter { it.memberId == memberId || it.memberId == "default_member" }
            .sortedByDescending { it.loggedAtEpochMs }
    }

    override suspend fun deleteFoodLog(id: String) {
        logs.remove(id)
        flow.value = logs.values.toList()
    }

    override suspend fun getUnsyncedFoodLogs(): List<FoodLogEntity> = logs.values.filter { !it.isSynced }

    override suspend fun markFoodLogSynced(id: String, synced: Boolean) {
        logs[id]?.let { logs[id] = it.copy(isSynced = synced) }
    }
}

class FoodNutritionTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testFoodDao_logAndMacroSummation() = runBlocking {
        val dao = FakeFoodDao()
        val now = System.currentTimeMillis()
        val startOfDay = now - 10000

        val log1 = FoodLogEntity(
            id = UUID.randomUUID().toString(),
            memberId = "test_user",
            mealType = "BREAKFAST",
            foodName = "Oatmeal with Whey",
            servingSizeGrams = 150f,
            caloriesKcal = 380,
            proteinGrams = 32f,
            carbsGrams = 48f,
            fatGrams = 6f,
            loggedAtEpochMs = now
        )

        val log2 = FoodLogEntity(
            id = UUID.randomUUID().toString(),
            memberId = "test_user",
            mealType = "LUNCH",
            foodName = "Chicken & Brown Rice",
            servingSizeGrams = 250f,
            caloriesKcal = 520,
            proteinGrams = 54f,
            carbsGrams = 58f,
            fatGrams = 7f,
            loggedAtEpochMs = now
        )

        dao.upsertFoodLog(log1)
        dao.upsertFoodLog(log2)

        val totalCalories = dao.getTodayCaloriesTotal("test_user", startOfDay).first()
        assertEquals(900, totalCalories)

        val totalProtein = dao.getTodayProteinTotal("test_user", startOfDay).first()
        assertEquals(86f, totalProtein ?: 0f, 0.01f)

        dao.deleteFoodLog(log1.id)
        val remainingCalories = dao.getTodayCaloriesTotal("test_user", startOfDay).first()
        assertEquals(520, remainingCalories)
    }

    @Test
    fun testOpenFoodFacts_jsonDeserialization() {
        val sampleJson = """
            {
              "status": 1,
              "status_verbose": "product found",
              "product": {
                "product_name": "Gold Standard 100% Whey",
                "brands": "Optimum Nutrition",
                "serving_size": "30.4 g",
                "nutriments": {
                  "energy-kcal_100g": 374.0,
                  "proteins_100g": 78.9,
                  "carbohydrates_100g": 5.3,
                  "fat_100g": 3.3,
                  "fiber_100g": 1.0
                }
              }
            }
        """.trimIndent()

        val parsed = json.decodeFromString<OpenFoodFactsResponse>(sampleJson)
        assertEquals(1, parsed.status)
        assertNotNull(parsed.product)
        assertEquals("Gold Standard 100% Whey", parsed.product?.productName)
        assertEquals("Optimum Nutrition", parsed.product?.brands)
        assertEquals(78.9, parsed.product?.nutriments?.proteins100g ?: 0.0, 0.01)
    }

    @Test
    fun testTargetFatLossCatalog_allZonesConfigured() {
        val allZones = TargetFatLossCatalog.zones
        assertTrue("Expected 7 body fat zones", allZones.size >= 7)

        val frontZones = TargetFatLossCatalog.getZonesByView(BodyView.FRONT)
        val backZones = TargetFatLossCatalog.getZonesByView(BodyView.BACK)

        assertTrue(frontZones.isNotEmpty())
        assertTrue(backZones.isNotEmpty())

        val bellyZone = TargetFatLossCatalog.getZoneById("belly_core")
        assertNotNull(bellyZone)
        assertTrue(bellyZone!!.hiitCircuitExercises.isNotEmpty())
        assertTrue(bellyZone.firmingRoutineExercises.isNotEmpty())
        assertTrue(bellyZone.nutritionKeyAdvice.isNotEmpty())
    }
}
