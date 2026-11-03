package com.gymflow.data.repository

import com.gymflow.core.common.Resource
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.network.GymFlowApiClient
import com.gymflow.core.network.LoginRequest
import com.gymflow.core.network.RegisterRequest
import com.gymflow.core.security.TokenManager
import com.gymflow.data.sync.SyncManager
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import com.gymflow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val apiClient: GymFlowApiClient,
    private val syncManager: SyncManager
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        val response = apiClient.login(LoginRequest(email = email, password = password))
        if (response.success && response.data != null) {
            val authData = response.data
            val user = authData.user
            tokenManager.saveAuth(
                token = authData.token,
                userId = user.id,
                email = user.email,
                name = user.name,
                role = user.role
            )

            // Cache in Room
            val userEntity = UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role,
                token = authData.token,
                heightCm = authData.memberProfile?.heightCm ?: 178f,
                currentWeightKg = authData.memberProfile?.currentWeightKg ?: 67.8f,
                fitnessGoal = authData.memberProfile?.fitnessGoal ?: "Build Muscle",
                age = authData.memberProfile?.age ?: 28,
                connectedTrainerId = authData.memberProfile?.connectedTrainerId,
                syncStatus = authData.memberProfile?.syncStatus ?: "Connected"
            )
            userDao.upsertUser(userEntity)

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(user))
        } else {
            emit(Resource.Error(response.message ?: "Authentication failed"))
        }
    }

    override fun register(
        name: String,
        email: String,
        password: String,
        role: Role
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        val response = apiClient.register(
            RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = role
            )
        )
        if (response.success && response.data != null) {
            val authData = response.data
            val user = authData.user
            tokenManager.saveAuth(
                token = authData.token,
                userId = user.id,
                email = user.email,
                name = user.name,
                role = user.role
            )

            val userEntity = UserEntity(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role,
                token = authData.token
            )
            userDao.upsertUser(userEntity)

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(user))
        } else {
            emit(Resource.Error(response.message ?: "Registration failed"))
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUserFlow().map { it?.toDomain() }
    }

    override suspend fun getCurrentUserSync(): User? {
        return userDao.getCurrentUser()?.toDomain()
    }

    override fun getMemberProfile(userId: String): Flow<MemberProfile?> {
        return userDao.getUserFlow(userId).map { it?.toMemberProfile() }
    }

    override fun updateProfile(
        userId: String,
        heightCm: Float,
        weightKg: Float,
        fitnessGoal: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        // Update local cache immediately
        userDao.updateMemberProfile(userId, heightCm, weightKg, fitnessGoal)

        // Attempt remote update
        val response = apiClient.updateMemberProfile(
            MemberProfile(
                userId = userId,
                heightCm = heightCm,
                currentWeightKg = weightKg,
                fitnessGoal = fitnessGoal
            )
        )
        if (response.success) {
            emit(Resource.Success(Unit))
        } else {
            // Local update succeeded even if network is offline
            emit(Resource.Success(Unit))
        }
    }

    override fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        tokenManager.clearAll()
        userDao.clearUsers()
        syncManager.cancelAllSync()
        emit(Resource.Success(Unit))
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.isAuthenticated()
    }
}
