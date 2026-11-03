package com.gymflow.server.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    MEMBER,
    TRAINER
}

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val token: String? = null
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "MEMBER",
    val heightCm: Float? = null,
    val currentWeightKg: Float? = null,
    val goal: String? = null,
    val age: Int? = null,
    val experienceYears: Int? = null,
    val specialization: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class MemberProfileDto(
    val userId: String,
    val name: String? = null,
    val email: String? = null,
    val heightCm: Float,
    val currentWeightKg: Float,
    val fitnessGoal: String,
    val age: Int,
    val connectedTrainerId: String? = null,
    val syncStatus: String = "DISCONNECTED"
)

@Serializable
data class TrainerProfileDto(
    val userId: String,
    val name: String? = null,
    val email: String? = null,
    val experienceYears: Int,
    val specialization: String,
    val activeMembersCount: Int = 0,
    val totalMembersCount: Int = 0
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val category: String,
    val muscleGroup: String,
    val equipment: String,
    val instructions: String,
    val defaultRestSeconds: Int
)

@Serializable
data class WorkoutExerciseDto(
    val id: String? = null,
    val exerciseId: String,
    val exerciseName: String,
    val category: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val restDurationSeconds: Int = 45,
    val notes: String = ""
)

@Serializable
data class WorkoutDayDto(
    val id: String? = null,
    val dayOfWeek: String,
    val title: String,
    val isRestDay: Boolean = false,
    val exercises: List<WorkoutExerciseDto> = emptyList()
)

@Serializable
data class WorkoutPlanDto(
    val id: String,
    val trainerId: String,
    val memberId: String,
    val name: String,
    val version: Int = 1,
    val days: List<WorkoutDayDto> = emptyList(),
    val isActive: Boolean = true
)

@Serializable
data class CreatePlanRequest(
    val memberId: String,
    val name: String,
    val days: List<WorkoutDayDto>
)

@Serializable
data class WorkoutSessionDto(
    val id: String,
    val memberId: String,
    val planId: String? = null,
    val workoutTitle: String,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long? = null,
    val durationSeconds: Long = 0,
    val totalVolumeKg: Float = 0f,
    val caloriesBurned: Int = 0,
    val isCompleted: Boolean = false,
    val setsCompleted: Int = 0,
    val totalSets: Int = 0
)

@Serializable
data class StartWorkoutRequest(
    val workoutTitle: String,
    val planId: String? = null
)

@Serializable
data class CompleteWorkoutRequest(
    val durationSeconds: Long,
    val totalVolumeKg: Float,
    val caloriesBurned: Int
)

@Serializable
data class SetLogDto(
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val actualReps: Int,
    val actualWeightKg: Float,
    val completedAtEpochMs: Long
)

@Serializable
data class LogSetRequest(
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val actualReps: Int,
    val actualWeightKg: Float
)

@Serializable
data class WeightLogDto(
    val id: String,
    val memberId: String,
    val preWeightKg: Float,
    val postWeightKg: Float,
    val deltaKg: Float,
    val recordedAtEpochMs: Long,
    val notes: String = ""
)

@Serializable
data class RecordWeightRequest(
    val preWeightKg: Float,
    val postWeightKg: Float,
    val notes: String = ""
)

@Serializable
data class HydrationLogDto(
    val id: String,
    val memberId: String,
    val amountMl: Int,
    val recordedAtEpochMs: Long
)

@Serializable
data class RecordHydrationRequest(
    val amountMl: Int
)

@Serializable
data class BodyMeasurementDto(
    val id: String,
    val memberId: String,
    val chestCm: Float,
    val waistCm: Float,
    val armsCm: Float,
    val thighsCm: Float,
    val hipsCm: Float,
    val recordedAtEpochMs: Long
)

@Serializable
data class RecordBodyMeasurementRequest(
    val chestCm: Float,
    val waistCm: Float,
    val armsCm: Float,
    val thighsCm: Float,
    val hipsCm: Float
)

@Serializable
data class MemberProgressDto(
    val memberProfile: MemberProfileDto,
    val recentWeights: List<WeightLogDto>,
    val latestMeasurements: BodyMeasurementDto?,
    val totalWorkoutsCompleted: Int,
    val totalVolumeLiftedKg: Float,
    val recentSessions: List<WorkoutSessionDto>
)

@Serializable
data class InvitationCodeDto(
    val code: String,
    val trainerId: String,
    val trainerName: String,
    val expiresAtEpochMs: Long,
    val isUsed: Boolean = false
)

@Serializable
data class GenerateInvitationResponse(
    val code: String,
    val expiresAtEpochMs: Long
)

@Serializable
data class JoinTrainerRequest(
    val code: String
)

@Serializable
data class ConnectedMemberDto(
    val requestId: String,
    val memberId: String,
    val name: String,
    val email: String,
    val status: String, // "ACTIVE", "PENDING", "INACTIVE"
    val currentWorkout: String? = null,
    val lastActive: Long? = null,
    val goal: String? = null
)

@Serializable
data class SyncChangeDto(
    val id: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val version: Long,
    val payload: String,
    val timestamp: Long
)

@Serializable
data class SyncPushRequest(
    val changes: List<SyncChangeDto>
)

@Serializable
data class SyncPullResponse(
    val changes: List<SyncChangeDto>,
    val lastSyncTimestamp: Long
)

@Serializable
data class LiveWorkoutEvent(
    val type: String, // "WORKOUT_START", "SET_COMPLETED", "WORKOUT_FINISH", "PING", "PONG"
    val memberId: String,
    val memberName: String,
    val trainerId: String? = null,
    val workoutTitle: String? = null,
    val exerciseName: String? = null,
    val setNumber: Int? = null,
    val actualReps: Int? = null,
    val actualWeightKg: Float? = null,
    val totalVolumeKg: Float? = null,
    val durationSeconds: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SimpleResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val status: Int
)
