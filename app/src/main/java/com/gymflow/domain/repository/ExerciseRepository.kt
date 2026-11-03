package com.gymflow.domain.repository

import com.gymflow.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercises(): Flow<List<Exercise>>
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>
    fun getExercisesByCategoryAndEquipment(category: String, equipment: String): Flow<List<Exercise>>
    fun searchExercises(query: String): Flow<List<Exercise>>
    fun getExerciseById(id: String): Flow<Exercise?>
    suspend fun seedDefaultCatalogIfEmpty()
    suspend fun syncExercisesWithFirestore()
    suspend fun saveCustomExercise(exercise: Exercise)
}

