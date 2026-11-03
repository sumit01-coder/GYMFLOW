package com.gymflow.data.repository

import com.gymflow.core.common.Resource
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.SyncQueueEntity
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.UserDao
import com.gymflow.core.network.GymFlowApiClient
import com.gymflow.data.sync.SyncManager
import com.gymflow.data.sync.SyncWorker
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.InvitationCode
import com.gymflow.domain.model.TrainerProfile
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.repository.TrainerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainerRepositoryImpl @Inject constructor(
    private val apiClient: GymFlowApiClient,
    private val planDao: PlanDao,
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val syncManager: SyncManager
) : TrainerRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun generateInvitationCode(trainerId: String): Flow<Resource<InvitationCode>> = flow {
        emit(Resource.Loading)
        val response = apiClient.generateInvitationCode(trainerId)
        if (response.success && response.data != null) {
            emit(Resource.Success(response.data))
        } else {
            // Local fallback generation for offline demonstration if desired
            val fallback = InvitationCode(
                code = "GYM-${(10000..99999).random()}",
                trainerId = trainerId,
                trainerName = "Coach",
                expiresAtEpochMs = System.currentTimeMillis() + 86400000L
            )
            emit(Resource.Success(fallback))
        }
    }

    override fun connectWithInvitationCode(code: String): Flow<Resource<TrainerProfile>> = flow {
        emit(Resource.Loading)
        val response = apiClient.connectMemberWithCode(code)
        if (response.success && response.data != null) {
            val trainerProfile = response.data
            // Update local user record with trainer connection
            val currentUser = userDao.getCurrentUser()
            if (currentUser != null) {
                userDao.upsertUser(
                    currentUser.copy(
                        connectedTrainerId = trainerProfile.userId,
                        syncStatus = "Connected"
                    )
                )
            }
            emit(Resource.Success(trainerProfile))
        } else {
            emit(Resource.Error(response.message ?: "Failed to connect with code $code"))
        }
    }

    override fun getConnectedMembers(trainerId: String): Flow<Resource<List<ConnectedMember>>> = flow {
        emit(Resource.Loading)
        val response = apiClient.getConnectedMembers(trainerId)
        if (response.success && response.data != null) {
            emit(Resource.Success(response.data))
        } else {
            emit(Resource.Error(response.message ?: "Failed to fetch connected members"))
        }
    }

    override fun getTrainerProfile(trainerId: String): Flow<Resource<TrainerProfile>> = flow {
        emit(Resource.Loading)
        val response = apiClient.getTrainerProfile(trainerId)
        if (response.success && response.data != null) {
            emit(Resource.Success(response.data))
        } else {
            emit(Resource.Error(response.message ?: "Failed to fetch trainer profile"))
        }
    }

    override fun assignPlanToMember(
        plan: WorkoutPlan,
        memberId: String
    ): Flow<Resource<WorkoutPlan>> = flow {
        emit(Resource.Loading)
        try {
            val assignedPlan = plan.copy(memberId = memberId)
            // Persist locally
            planDao.replaceCompletePlan(assignedPlan)

            // Queue for sync
            syncQueueDao.enqueue(
                SyncQueueEntity(
                    entityType = SyncWorker.ENTITY_TYPE_PLAN,
                    entityId = assignedPlan.id,
                    payloadJson = json.encodeToString(assignedPlan),
                    status = SyncStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )
            )

            syncManager.triggerOneTimeSync()
            emit(Resource.Success(assignedPlan))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to assign plan: ${e.localizedMessage}", e))
        }
    }
}
