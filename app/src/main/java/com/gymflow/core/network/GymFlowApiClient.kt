package com.gymflow.core.network

import android.util.Log
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.InvitationCode
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.TrainerProfile
import com.gymflow.domain.model.User
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton

interface GymFlowApiClient {
    // Auth
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse>
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse>
    suspend fun getProfile(): ApiResponse<User>

    // Plans
    suspend fun getActivePlan(memberId: String): ApiResponse<WorkoutPlan>
    suspend fun getPlanById(planId: String): ApiResponse<WorkoutPlan>
    suspend fun createOrUpdatePlan(plan: WorkoutPlan): ApiResponse<WorkoutPlan>
    suspend fun getTrainerPlans(trainerId: String): ApiResponse<List<WorkoutPlan>>

    // Workout Sessions
    suspend fun syncWorkoutSession(request: SyncSessionRequest): ApiResponse<WorkoutSession>
    suspend fun getRecentSessions(memberId: String): ApiResponse<List<WorkoutSession>>

    // Weight, Hydration & Body Metrics
    suspend fun logWeight(log: WeightLog): ApiResponse<WeightLog>
    suspend fun getWeightHistory(memberId: String): ApiResponse<List<WeightLog>>
    suspend fun logHydration(log: HydrationLog): ApiResponse<HydrationLog>
    suspend fun getHydrationLogs(memberId: String): ApiResponse<List<HydrationLog>>
    suspend fun logBodyMeasurement(measurement: BodyMeasurement): ApiResponse<BodyMeasurement>
    suspend fun getMeasurements(memberId: String): ApiResponse<List<BodyMeasurement>>

    // Trainer & Member Connections
    val baseUrl: String
    suspend fun generateInvitationCode(trainerId: String): ApiResponse<InvitationCode>
    suspend fun connectMemberWithCode(code: String): ApiResponse<TrainerProfile>
    suspend fun getConnectedMembers(trainerId: String): ApiResponse<List<ConnectedMember>>
    suspend fun getTrainerProfile(trainerId: String): ApiResponse<TrainerProfile>
    suspend fun getMemberProfile(memberId: String): ApiResponse<MemberProfile>
    suspend fun updateMemberProfile(profile: MemberProfile): ApiResponse<MemberProfile>

    // Batch Sync
    suspend fun syncBatch(request: BatchSyncRequest): ApiResponse<BatchSyncResponse>
}

@Singleton
class GymFlowApiClientImpl @Inject constructor(
    private val httpClient: HttpClient
) : GymFlowApiClient {

    override val baseUrl: String = KtorClientFactory.DEFAULT_BASE_URL
    private val tag = "GymFlowApiClient"

    private suspend inline fun <reified T> safeCall(call: () -> io.ktor.client.statement.HttpResponse): ApiResponse<T> {
        return try {
            val response = call()
            if (response.status.isSuccess()) {
                response.body<ApiResponse<T>>()
            } else {
                val errorBody = try {
                    response.body<ApiResponse<T>>()
                } catch (_: Exception) {
                    null
                }
                ApiResponse(
                    success = false,
                    data = null,
                    message = errorBody?.message ?: "HTTP Error ${response.status.value}: ${response.status.description}",
                    errorCode = response.status.value.toString()
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Network call failed", e)
            ApiResponse(
                success = false,
                data = null,
                message = e.localizedMessage ?: "Unknown network error",
                errorCode = "NETWORK_ERROR"
            )
        }
    }

    override suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> = safeCall {
        httpClient.post("api/auth/login") {
            setBody(request)
        }
    }

    override suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> = safeCall {
        httpClient.post("api/auth/register") {
            setBody(request)
        }
    }

    override suspend fun getProfile(): ApiResponse<User> = safeCall {
        httpClient.get("api/auth/profile")
    }

    override suspend fun getActivePlan(memberId: String): ApiResponse<WorkoutPlan> = safeCall {
        httpClient.get("api/plans/active") {
            parameter("memberId", memberId)
        }
    }

    override suspend fun getPlanById(planId: String): ApiResponse<WorkoutPlan> = safeCall {
        httpClient.get("api/plans/$planId")
    }

    override suspend fun createOrUpdatePlan(plan: WorkoutPlan): ApiResponse<WorkoutPlan> = safeCall {
        httpClient.post("api/plans") {
            setBody(plan)
        }
    }

    override suspend fun getTrainerPlans(trainerId: String): ApiResponse<List<WorkoutPlan>> = safeCall {
        httpClient.get("api/plans/trainer/$trainerId")
    }

    override suspend fun syncWorkoutSession(request: SyncSessionRequest): ApiResponse<WorkoutSession> = safeCall {
        httpClient.post("api/workouts/sync-session") {
            setBody(request)
        }
    }

    override suspend fun getRecentSessions(memberId: String): ApiResponse<List<WorkoutSession>> = safeCall {
        httpClient.get("api/workouts/sessions") {
            parameter("memberId", memberId)
        }
    }

    override suspend fun logWeight(log: WeightLog): ApiResponse<WeightLog> = safeCall {
        httpClient.post("api/metrics/weight") {
            setBody(log)
        }
    }

    override suspend fun getWeightHistory(memberId: String): ApiResponse<List<WeightLog>> = safeCall {
        httpClient.get("api/metrics/weight") {
            parameter("memberId", memberId)
        }
    }

    override suspend fun logHydration(log: HydrationLog): ApiResponse<HydrationLog> = safeCall {
        httpClient.post("api/metrics/hydration") {
            setBody(log)
        }
    }

    override suspend fun getHydrationLogs(memberId: String): ApiResponse<List<HydrationLog>> = safeCall {
        httpClient.get("api/metrics/hydration") {
            parameter("memberId", memberId)
        }
    }

    override suspend fun logBodyMeasurement(measurement: BodyMeasurement): ApiResponse<BodyMeasurement> = safeCall {
        httpClient.post("api/metrics/measurements") {
            setBody(measurement)
        }
    }

    override suspend fun getMeasurements(memberId: String): ApiResponse<List<BodyMeasurement>> = safeCall {
        httpClient.get("api/metrics/measurements") {
            parameter("memberId", memberId)
        }
    }

    override suspend fun generateInvitationCode(trainerId: String): ApiResponse<InvitationCode> = safeCall {
        httpClient.post("api/trainer/invite-code") {
            parameter("trainerId", trainerId)
        }
    }

    override suspend fun connectMemberWithCode(code: String): ApiResponse<TrainerProfile> = safeCall {
        httpClient.post("api/member/connect") {
            setBody(ConnectCodeRequest(code))
        }
    }

    override suspend fun getConnectedMembers(trainerId: String): ApiResponse<List<ConnectedMember>> = safeCall {
        httpClient.get("api/trainer/$trainerId/members")
    }

    override suspend fun getTrainerProfile(trainerId: String): ApiResponse<TrainerProfile> = safeCall {
        httpClient.get("api/trainer/$trainerId/profile")
    }

    override suspend fun getMemberProfile(memberId: String): ApiResponse<MemberProfile> = safeCall {
        httpClient.get("api/member/$memberId/profile")
    }

    override suspend fun updateMemberProfile(profile: MemberProfile): ApiResponse<MemberProfile> = safeCall {
        httpClient.put("api/member/profile") {
            setBody(profile)
        }
    }

    override suspend fun syncBatch(request: BatchSyncRequest): ApiResponse<BatchSyncResponse> = safeCall {
        httpClient.post("api/sync/batch") {
            setBody(request)
        }
    }
}
