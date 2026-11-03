package com.gymflow.domain.repository

import com.gymflow.core.common.Resource
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>

    fun register(
        name: String,
        email: String,
        password: String,
        role: Role
    ): Flow<Resource<User>>

    fun getCurrentUser(): Flow<User?>

    suspend fun getCurrentUserSync(): User?

    fun getMemberProfile(userId: String): Flow<MemberProfile?>

    fun updateProfile(
        userId: String,
        heightCm: Float,
        weightKg: Float,
        fitnessGoal: String
    ): Flow<Resource<Unit>>

    fun logout(): Flow<Resource<Unit>>

    fun isLoggedIn(): Flow<Boolean>
}
