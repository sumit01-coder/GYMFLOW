package com.gymflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    MEMBER,
    TRAINER
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val token: String? = null
)

@Serializable
data class MemberProfile(
    val userId: String,
    val heightCm: Float = 178f,
    val currentWeightKg: Float = 67.8f,
    val fitnessGoal: String = "Build Muscle",
    val age: Int = 28,
    val connectedTrainerId: String? = null,
    val syncStatus: String = "Connected"
)

@Serializable
data class TrainerProfile(
    val userId: String,
    val experienceYears: Int = 5,
    val specialization: String = "Hypertrophy & Strength",
    val activeMembersCount: Int = 24,
    val totalMembersCount: Int = 32
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val category: String, // Chest, Back, Legs, Shoulders, Arms, etc.
    val muscleGroup: String,
    val equipment: String, // Barbell, Dumbbell, Machine, Bodyweight
    val instructions: List<String> = emptyList(),
    val defaultRestSeconds: Int = 45,
    val mediaUrl: String? = null
)

@Serializable
data class ExerciseSet(
    val setNumber: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val actualReps: Int? = null,
    val actualWeightKg: Float? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class WorkoutExercise(
    val exerciseId: String,
    val exerciseName: String,
    val category: String,
    val targetSets: Int,
    val targetReps: Int,
    val targetWeightKg: Float,
    val restDurationSeconds: Int = 45,
    val notes: String = "",
    val sets: List<ExerciseSet> = emptyList()
)

@Serializable
data class WorkoutDay(
    val dayOfWeek: String, // Monday, Tuesday, ...
    val title: String, // e.g. "Chest + Triceps"
    val exercises: List<WorkoutExercise> = emptyList(),
    val isCompleted: Boolean = false,
    val isRestDay: Boolean = false
)

@Serializable
data class WorkoutPlan(
    val id: String,
    val trainerId: String,
    val memberId: String,
    val name: String,
    val version: Int = 1,
    val days: List<WorkoutDay> = emptyList(),
    val isActive: Boolean = true
)

@Serializable
data class WorkoutSession(
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
data class SetLog(
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
data class WeightLog(
    val id: String,
    val memberId: String,
    val preWeightKg: Float,
    val postWeightKg: Float,
    val deltaKg: Float = postWeightKg - preWeightKg,
    val recordedAtEpochMs: Long,
    val notes: String = ""
)

@Serializable
data class BodyMeasurement(
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
data class HydrationLog(
    val id: String,
    val memberId: String,
    val amountMl: Int,
    val dailyGoalMl: Int = 3000,
    val recordedAtEpochMs: Long
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAtEpochMs: Long? = null
)

@Serializable
data class InvitationCode(
    val code: String, // e.g. "GYM-82K91"
    val trainerId: String,
    val trainerName: String,
    val expiresAtEpochMs: Long,
    val isUsed: Boolean = false
)

@Serializable
data class ConnectedMember(
    val memberId: String,
    val name: String,
    val avatarUrl: String? = null,
    val currentWorkout: String,
    val lastActive: String,
    val status: String // "Active", "Pending", "Inactive"
)

