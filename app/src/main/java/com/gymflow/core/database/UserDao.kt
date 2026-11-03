package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY lastUpdated DESC LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("UPDATE users SET heightCm = :heightCm, currentWeightKg = :weightKg, fitnessGoal = :goal, lastUpdated = :now WHERE id = :id")
    suspend fun updateMemberProfile(
        id: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        now: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
