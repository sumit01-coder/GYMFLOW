package com.gymflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DetectedFoodItem(
    val name: String,
    val estimatedGrams: Float = 100f,
    val caloriesKcal: Int = 0,
    val proteinGrams: Float = 0f,
    val carbsGrams: Float = 0f,
    val fatGrams: Float = 0f,
    val fiberGrams: Float = 0f
)

@Serializable
data class AiFoodVisionResult(
    val mealTitle: String,
    val items: List<DetectedFoodItem> = emptyList(),
    val totalCaloriesKcal: Int = 0,
    val totalProteinGrams: Float = 0f,
    val totalCarbsGrams: Float = 0f,
    val totalFatGrams: Float = 0f,
    val totalFiberGrams: Float = 0f,
    val healthAdvice: String = ""
)
