package com.gymflow.core.network

import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.InvitationCode
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.TrainerProfile
import com.gymflow.domain.model.User
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: Role
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
    val memberProfile: MemberProfile? = null,
    val trainerProfile: TrainerProfile? = null,
    val message: String? = null
)

@Serializable
data class SyncSessionRequest(
    val session: WorkoutSession,
    val setLogs: List<SetLog> = emptyList()
)

@Serializable
data class ConnectCodeRequest(
    val code: String
)

@Serializable
data class BatchSyncItem(
    val queueId: Long,
    val entityType: String,
    val entityId: String,
    val payloadJson: String,
    val timestamp: Long
)

@Serializable
data class BatchSyncRequest(
    val items: List<BatchSyncItem>
)

@Serializable
data class BatchSyncResponse(
    val successfulQueueIds: List<Long>,
    val failedQueueIds: List<Long> = emptyList(),
    val message: String? = null
)
