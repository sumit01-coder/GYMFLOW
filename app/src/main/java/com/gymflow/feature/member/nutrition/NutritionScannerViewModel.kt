package com.gymflow.feature.member.nutrition

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.FoodLogEntity
import com.gymflow.core.network.OpenFoodProduct
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.AiFoodVisionResult
import com.gymflow.domain.repository.DailyMacros
import com.gymflow.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

enum class ScanMode {
    BARCODE,
    AI_PHOTO
}

data class NutritionScannerUiState(
    val scanMode: ScanMode = ScanMode.BARCODE,
    val isAnalyzing: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val scannedProduct: OpenFoodProduct? = null,
    val aiVisionResult: AiFoodVisionResult? = null,
    val servingWeightGrams: Float = 100f,
    val selectedMealType: String = "LUNCH",
    val todayCalories: Int = 0,
    val todayMacros: DailyMacros = DailyMacros()
)

@HiltViewModel
class NutritionScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionScannerUiState())
    val uiState: StateFlow<NutritionScannerUiState> = _uiState.asStateFlow()

    init {
        observeTodayNutrition()
    }

    private fun observeTodayNutrition() {
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        val startOfDayMs = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            foodRepository.getTodayMacros(memberId, startOfDayMs).collect { macros ->
                _uiState.update { it.copy(todayMacros = macros, todayCalories = macros.totalCaloriesKcal) }
            }
        }
    }

    fun onSwitchScanMode(mode: ScanMode) {
        _uiState.update {
            it.copy(
                scanMode = mode,
                errorMessage = null,
                scannedProduct = null,
                aiVisionResult = null
            )
        }
    }

    fun onToggleFlash() {
        _uiState.update { it.copy(isFlashEnabled = !it.isFlashEnabled) }
    }

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.isAnalyzing || _uiState.value.scannedProduct != null) return

        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            val result = foodRepository.scanBarcode(barcode)
            result.onSuccess { product ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        scannedProduct = product,
                        servingWeightGrams = 100f
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Product not found: ${err.message}"
                    )
                }
            }
        }
    }

    fun onAnalyzePhoto(bitmap: Bitmap) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            val result = foodRepository.analyzeFoodImage(bitmap)
            result.onSuccess { vision ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        aiVisionResult = vision
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Failed to analyze meal: ${err.message}"
                    )
                }
            }
        }
    }

    fun onUpdateServingWeight(grams: Float) {
        _uiState.update { it.copy(servingWeightGrams = grams.coerceAtLeast(10f)) }
    }

    fun onSelectMealType(mealType: String) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun onSaveScannedProduct() {
        val product = _uiState.value.scannedProduct ?: return
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        val grams = _uiState.value.servingWeightGrams
        val ratio = grams / 100f

        val nutriments = product.nutriments
        val kcal100 = (nutriments?.energyKcal100g ?: nutriments?.energyKcal ?: 0.0).toFloat()
        val p100 = (nutriments?.proteins100g ?: 0.0).toFloat()
        val c100 = (nutriments?.carbohydrates100g ?: 0.0).toFloat()
        val f100 = (nutriments?.fat100g ?: 0.0).toFloat()
        val fiber100 = (nutriments?.fiber100g ?: 0.0).toFloat()

        val foodEntity = FoodLogEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            mealType = _uiState.value.selectedMealType,
            foodName = product.productName ?: "Packaged Food",
            brand = product.brands,
            barcode = null,
            servingSizeGrams = grams,
            caloriesKcal = (kcal100 * ratio).toInt(),
            proteinGrams = p100 * ratio,
            carbsGrams = c100 * ratio,
            fatGrams = f100 * ratio,
            fiberGrams = fiber100 * ratio,
            imageUrl = product.imageFrontUrl ?: product.imageUrl,
            source = "BARCODE_OFF",
            loggedAtEpochMs = System.currentTimeMillis()
        )

        viewModelScope.launch {
            foodRepository.logFood(foodEntity)
            _uiState.update {
                it.copy(
                    scannedProduct = null,
                    successMessage = "Logged ${foodEntity.foodName} (${foodEntity.caloriesKcal} kcal) to ${_uiState.value.selectedMealType}!"
                )
            }
        }
    }

    fun onSaveAiVisionMeal() {
        val vision = _uiState.value.aiVisionResult ?: return
        val memberId = tokenManager.getUserIdSync() ?: "default_member"

        val foodEntity = FoodLogEntity(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            mealType = _uiState.value.selectedMealType,
            foodName = vision.mealTitle,
            brand = "AI Vision Detected",
            barcode = null,
            servingSizeGrams = vision.items.sumOf { it.estimatedGrams.toDouble() }.toFloat(),
            caloriesKcal = vision.totalCaloriesKcal,
            proteinGrams = vision.totalProteinGrams,
            carbsGrams = vision.totalCarbsGrams,
            fatGrams = vision.totalFatGrams,
            fiberGrams = vision.totalFiberGrams,
            imageUrl = null,
            source = "AI_VISION",
            loggedAtEpochMs = System.currentTimeMillis()
        )

        viewModelScope.launch {
            foodRepository.logFood(foodEntity)
            _uiState.update {
                it.copy(
                    aiVisionResult = null,
                    successMessage = "Logged ${foodEntity.foodName} (${foodEntity.caloriesKcal} kcal) to ${_uiState.value.selectedMealType}!"
                )
            }
        }
    }

    fun onDismissCard() {
        _uiState.update {
            it.copy(
                scannedProduct = null,
                aiVisionResult = null,
                errorMessage = null
            )
        }
    }

    fun onClearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
