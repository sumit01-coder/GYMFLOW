package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val mealType: String, // BREAKFAST, LUNCH, DINNER, SNACK
    val foodName: String,
    val brand: String? = null,
    val barcode: String? = null,
    val servingSizeGrams: Float = 100f,
    val caloriesKcal: Int = 0,
    val proteinGrams: Float = 0f,
    val carbsGrams: Float = 0f,
    val fatGrams: Float = 0f,
    val fiberGrams: Float = 0f,
    val sugarsGrams: Float = 0f,
    val sodiumMg: Float = 0f,
    val imageUrl: String? = null,
    val source: String = "MANUAL", // BARCODE_OFF, AI_VISION, MANUAL
    val loggedAtEpochMs: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
