package com.gymflow.core.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.DailyStepsDao
import com.gymflow.core.database.DailyStepsEntity
import com.gymflow.core.database.GymMemberDao
import com.gymflow.core.database.GymMemberEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.OutdoorWorkoutDao
import com.gymflow.core.database.OutdoorWorkoutEntity
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.MembershipOrder
import com.gymflow.domain.model.OrderStatus
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class CloudTrainerCodeInfo(
    val code: String,
    val trainerId: String,
    val trainerName: String,
    val gymName: String,
    val expiresAtEpochMs: Long
)

data class CloudTrainerInfo(
    val id: String,
    val name: String,
    val gymName: String,
    val inviteCode: String,
    val upiId: String = ""
)

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val gymMemberDao: GymMemberDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightDao: WeightDao,
    private val planDao: PlanDao,
    private val dailyStepsDao: DailyStepsDao,
    private val outdoorWorkoutDao: OutdoorWorkoutDao
) {

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_TRAINERS = "trainers"
        const val COLLECTION_TRAINER_CODES = "trainer_invite_codes"
        const val COLLECTION_PLANS = "plans"
        const val COLLECTION_SESSIONS = "workout_sessions"
        const val COLLECTION_MEASUREMENTS = "body_measurements"
        const val COLLECTION_WEIGHT_LOGS = "weight_logs"
        const val COLLECTION_HYDRATION_LOGS = "hydration_logs"
        const val COLLECTION_DAILY_STEPS = "daily_steps"
        const val COLLECTION_OUTDOOR_WORKOUTS = "outdoor_workouts"
        const val COLLECTION_MEMBERSHIP_ORDERS = "membership_orders"
    }

    // 1. Sync User Profile
    suspend fun saveUserProfile(user: UserEntity) {
        val userMap = mapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "role" to user.role.name,
            "heightCm" to user.heightCm.toDouble(),
            "currentWeightKg" to user.currentWeightKg.toDouble(),
            "fitnessGoal" to user.fitnessGoal,
            "age" to user.age,
            "connectedTrainerId" to user.connectedTrainerId,
            "isEmailVerified" to user.isEmailVerified,
            "createdAtEpochMs" to user.createdAtEpochMs,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_USERS)
            .document(user.id)
            .set(userMap, SetOptions.merge())
            .await()
    }

    // Check if an email is already registered in Cloud Firestore (Primary Database)
    suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (_: Exception) {
            false
        }
    }

    // Retrieve User Profile directly from Cloud Firestore (Primary Database)
    suspend fun getUserProfileFromFirestore(userId: String): UserEntity? {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            if (!doc.exists()) return null

            val roleStr = doc.getString("role") ?: "MEMBER"
            val role = try { com.gymflow.domain.model.Role.valueOf(roleStr) } catch (_: Exception) { com.gymflow.domain.model.Role.MEMBER }

            UserEntity(
                id = doc.getString("id") ?: userId,
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                role = role,
                token = null,
                heightCm = (doc.getDouble("heightCm") ?: 0.0).toFloat(),
                currentWeightKg = (doc.getDouble("currentWeightKg") ?: 0.0).toFloat(),
                fitnessGoal = doc.getString("fitnessGoal") ?: "General Fitness",
                age = (doc.getLong("age") ?: 0L).toInt(),
                connectedTrainerId = doc.getString("connectedTrainerId"),
                syncStatus = "Connected",
                isEmailVerified = doc.getBoolean("isEmailVerified") ?: false,
                createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
    }

    // 2. Sync & Observe Workout Plans in Real-Time
    suspend fun saveWorkoutPlan(plan: WorkoutPlan) {
        val planMap = mapOf(
            "id" to plan.id,
            "trainerId" to plan.trainerId,
            "memberId" to plan.memberId,
            "name" to plan.name,
            "version" to plan.version,
            "isActive" to plan.isActive,
            "updatedAt" to FieldValue.serverTimestamp(),
            "days" to plan.days.map { day ->
                mapOf(
                    "dayOfWeek" to day.dayOfWeek,
                    "title" to day.title,
                    "isCompleted" to day.isCompleted,
                    "isRestDay" to day.isRestDay,
                    "exercises" to day.exercises.map { ex ->
                        mapOf(
                            "exerciseId" to ex.exerciseId,
                            "exerciseName" to ex.exerciseName,
                            "category" to ex.category,
                            "targetSets" to ex.targetSets,
                            "targetReps" to ex.targetReps,
                            "targetWeightKg" to ex.targetWeightKg.toDouble(),
                            "restDurationSeconds" to ex.restDurationSeconds,
                            "notes" to ex.notes
                        )
                    }
                )
            }
        )

        firestore.collection(COLLECTION_PLANS)
            .document(plan.id)
            .set(planMap, SetOptions.merge())
            .await()
    }

    fun observeMemberPlanRealtime(memberId: String): Flow<WorkoutPlan?> = callbackFlow {
        val registration = firestore.collection(COLLECTION_PLANS)
            .whereEqualTo("memberId", memberId)
            .whereEqualTo("isActive", true)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val doc = snapshot?.documents?.firstOrNull()
                if (doc == null || !doc.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                try {
                    val daysList = (doc.get("days") as? List<*>)?.mapNotNull { dayObj ->
                        val dayMap = dayObj as? Map<*, *> ?: return@mapNotNull null
                        val exList = (dayMap["exercises"] as? List<*>)?.mapNotNull { exObj ->
                            val exMap = exObj as? Map<*, *> ?: return@mapNotNull null
                            WorkoutExercise(
                                exerciseId = exMap["exerciseId"] as? String ?: "",
                                exerciseName = exMap["exerciseName"] as? String ?: "",
                                category = exMap["category"] as? String ?: "",
                                targetSets = (exMap["targetSets"] as? Number)?.toInt() ?: 3,
                                targetReps = (exMap["targetReps"] as? Number)?.toInt() ?: 12,
                                targetWeightKg = (exMap["targetWeightKg"] as? Number)?.toFloat() ?: 20f,
                                restDurationSeconds = (exMap["restDurationSeconds"] as? Number)?.toInt() ?: 45,
                                notes = exMap["notes"] as? String ?: ""
                            )
                        } ?: emptyList()

                        WorkoutDay(
                            dayOfWeek = dayMap["dayOfWeek"] as? String ?: "Monday",
                            title = dayMap["title"] as? String ?: "Workout",
                            isCompleted = dayMap["isCompleted"] as? Boolean ?: false,
                            isRestDay = dayMap["isRestDay"] as? Boolean ?: false,
                            exercises = exList
                        )
                    } ?: emptyList()

                    val parsedPlan = WorkoutPlan(
                        id = doc.getString("id") ?: doc.id,
                        trainerId = doc.getString("trainerId") ?: "",
                        memberId = doc.getString("memberId") ?: memberId,
                        name = doc.getString("name") ?: "Custom Workout Plan",
                        version = doc.getLong("version")?.toInt() ?: 1,
                        isActive = doc.getBoolean("isActive") ?: true,
                        days = daysList
                    )
                    trySend(parsedPlan)
                } catch (e: Exception) {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    // 3. Sync Workout Sessions & Sets
    suspend fun saveWorkoutSessionWithSets(session: WorkoutSessionEntity, setLogs: List<SetLogEntity>) {
        val sessionMap = mapOf(
            "id" to session.id,
            "memberId" to session.memberId,
            "planId" to session.planId,
            "workoutTitle" to session.workoutTitle,
            "startTimeEpochMs" to session.startTimeEpochMs,
            "endTimeEpochMs" to session.endTimeEpochMs,
            "durationSeconds" to session.durationSeconds,
            "totalVolumeKg" to session.totalVolumeKg.toDouble(),
            "caloriesBurned" to session.caloriesBurned,
            "isCompleted" to session.isCompleted,
            "setsCompleted" to session.setsCompleted,
            "totalSets" to session.totalSets,
            "syncedAt" to FieldValue.serverTimestamp(),
            "setLogs" to setLogs.map { log ->
                mapOf(
                    "id" to log.id,
                    "sessionId" to log.sessionId,
                    "exerciseId" to log.exerciseId,
                    "exerciseName" to log.exerciseName,
                    "setNumber" to log.setNumber,
                    "targetReps" to log.targetReps,
                    "targetWeightKg" to log.targetWeightKg.toDouble(),
                    "actualReps" to log.actualReps,
                    "actualWeightKg" to log.actualWeightKg.toDouble(),
                    "completedAtEpochMs" to log.completedAtEpochMs
                )
            }
        )

        firestore.collection(COLLECTION_SESSIONS)
            .document(session.id)
            .set(sessionMap, SetOptions.merge())
            .await()
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        val sessionMap = mapOf(
            "id" to session.id,
            "memberId" to session.memberId,
            "planId" to session.planId,
            "workoutTitle" to session.workoutTitle,
            "durationSeconds" to session.durationSeconds,
            "totalVolumeKg" to session.totalVolumeKg.toDouble(),
            "caloriesBurned" to session.caloriesBurned,
            "completedAt" to FieldValue.serverTimestamp(),
            "isCompleted" to session.isCompleted
        )

        firestore.collection(COLLECTION_SESSIONS)
            .document(session.id)
            .set(sessionMap, SetOptions.merge())
            .await()
    }

    suspend fun deleteWorkoutSession(sessionId: String) {
        try {
            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .delete()
                .await()
        } catch (_: Exception) {}
    }

    suspend fun deleteIncompleteSessions(userId: String) {
        try {
            val userSessions = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("memberId", userId)
                .whereEqualTo("isCompleted", false)
                .get()
                .await()
            for (doc in userSessions.documents) {
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
            if (userId != "default_member") {
                val defaultSessions = firestore.collection(COLLECTION_SESSIONS)
                    .whereEqualTo("memberId", "default_member")
                    .whereEqualTo("isCompleted", false)
                    .get()
                    .await()
                for (doc in defaultSessions.documents) {
                    try { doc.reference.delete().await() } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    // 4. Sync Weight Logs
    suspend fun saveWeightLog(log: WeightLogEntity) {
        val weightMap = mapOf(
            "id" to log.id,
            "memberId" to log.memberId,
            "preWeightKg" to (log.preWeightKg?.toDouble() ?: 0.0),
            "postWeightKg" to (log.postWeightKg?.toDouble() ?: 0.0),
            "deltaKg" to log.deltaKg.toDouble(),
            "recordedAtEpochMs" to log.recordedAtEpochMs,
            "postRecordedAtEpochMs" to log.postRecordedAtEpochMs,
            "notes" to log.notes,
            "syncedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_WEIGHT_LOGS)
            .document(log.id)
            .set(weightMap, SetOptions.merge())
            .await()
    }

    suspend fun deleteWeightLog(logId: String) {
        try {
            firestore.collection(COLLECTION_WEIGHT_LOGS)
                .document(logId)
                .delete()
                .await()
        } catch (_: Exception) {}
    }

    // 5. Sync Hydration Logs
    suspend fun saveHydrationLog(log: HydrationLogEntity) {
        val hydrationMap = mapOf(
            "id" to log.id,
            "memberId" to log.memberId,
            "amountMl" to log.amountMl,
            "recordedAtEpochMs" to log.recordedAtEpochMs,
            "syncedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_HYDRATION_LOGS)
            .document(log.id)
            .set(hydrationMap, SetOptions.merge())
            .await()
    }

    suspend fun deleteHydrationLog(logId: String) {
        try {
            firestore.collection(COLLECTION_HYDRATION_LOGS)
                .document(logId)
                .delete()
                .await()
        } catch (_: Exception) {}
    }

    suspend fun clearTodayHydrationLogs(memberId: String, startOfDayMs: Long) {
        try {
            val snapshot = firestore.collection(COLLECTION_HYDRATION_LOGS)
                .whereEqualTo("memberId", memberId)
                .whereGreaterThanOrEqualTo("recordedAtEpochMs", startOfDayMs)
                .get()
                .await()
            for (doc in snapshot.documents) {
                try {
                    doc.reference.delete().await()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    // 6. Sync Body Measurements
    suspend fun saveBodyMeasurement(measurement: BodyMeasurementEntity) {
        val measurementMap = mapOf(
            "id" to measurement.id,
            "memberId" to measurement.memberId,
            "chestCm" to measurement.chestCm.toDouble(),
            "waistCm" to measurement.waistCm.toDouble(),
            "armsCm" to measurement.armsCm.toDouble(),
            "thighsCm" to measurement.thighsCm.toDouble(),
            "hipsCm" to measurement.hipsCm.toDouble(),
            "recordedAtEpochMs" to measurement.recordedAtEpochMs,
            "syncedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_MEASUREMENTS)
            .document(measurement.id)
            .set(measurementMap, SetOptions.merge())
            .await()
    }

    // 7. Sync Daily Steps
    suspend fun saveDailySteps(steps: DailyStepsEntity) {
        val stepsMap = mapOf(
            "id" to steps.id,
            "memberId" to steps.memberId,
            "date" to steps.date,
            "stepCount" to steps.stepCount,
            "targetGoal" to steps.targetGoal,
            "distanceMeters" to steps.distanceMeters.toDouble(),
            "caloriesKcal" to steps.caloriesKcal,
            "activeMinutes" to steps.activeMinutes,
            "lastSensorStepCount" to steps.lastSensorStepCount,
            "lastUpdatedEpochMs" to steps.lastUpdatedEpochMs,
            "syncedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_DAILY_STEPS)
            .document(steps.id)
            .set(stepsMap, SetOptions.merge())
            .await()
    }

    // 8. Sync Outdoor Workout Session
    suspend fun saveOutdoorWorkout(workout: OutdoorWorkoutEntity) {
        val workoutMap = mapOf(
            "id" to workout.id,
            "memberId" to workout.memberId,
            "workoutType" to workout.workoutType,
            "startTimeEpochMs" to workout.startTimeEpochMs,
            "endTimeEpochMs" to workout.endTimeEpochMs,
            "durationSeconds" to workout.durationSeconds,
            "totalSteps" to workout.totalSteps,
            "totalDistanceMeters" to workout.totalDistanceMeters.toDouble(),
            "avgPaceSecondsPerKm" to workout.avgPaceSecondsPerKm.toDouble(),
            "maxSpeedMps" to workout.maxSpeedMps.toDouble(),
            "caloriesBurned" to workout.caloriesBurned,
            "polylineEncoded" to workout.polylineEncoded,
            "isCompleted" to workout.isCompleted,
            "syncedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_OUTDOOR_WORKOUTS)
            .document(workout.id)
            .set(workoutMap, SetOptions.merge())
            .await()
    }

    // 7. Comprehensive Master Sync: Upload All Local User Data to Firestore
    suspend fun syncAllLocalUserData(userId: String): Boolean {
        return try {
            val actualUserId = if (userId.isBlank() || userId == "default_member") {
                userDao.getCurrentUser()?.id ?: userId
            } else userId

            // 1. Sync User Profile
            val user = userDao.getUserById(actualUserId) ?: userDao.getCurrentUser()
            if (user != null) {
                saveUserProfile(user)
            }

            // 2. Sync Workout Sessions & Sets
            val unsyncedSessions = workoutSessionDao.getUnsyncedSessions()
            for (session in unsyncedSessions) {
                if (!session.isCompleted) continue
                val setLogs = workoutSessionDao.getSetLogsSync(session.id)
                saveWorkoutSessionWithSets(session, setLogs)
                workoutSessionDao.markSessionSynced(session.id, true)
                setLogs.forEach { log -> workoutSessionDao.markSetLogSynced(log.id, true) }
            }

            // 3. Sync Weight Logs
            val unsyncedWeights = weightDao.getUnsyncedWeightLogs()
            for (weightLog in unsyncedWeights) {
                saveWeightLog(weightLog)
                weightDao.markWeightLogSynced(weightLog.id, true)
            }

            // 4. Sync Hydration Logs
            val unsyncedHydration = weightDao.getUnsyncedHydrationLogs()
            for (hydrationLog in unsyncedHydration) {
                saveHydrationLog(hydrationLog)
                weightDao.markHydrationLogSynced(hydrationLog.id, true)
            }

            // 5. Sync Measurements
            val unsyncedMeasurements = weightDao.getUnsyncedMeasurements()
            for (measurement in unsyncedMeasurements) {
                saveBodyMeasurement(measurement)
                weightDao.markMeasurementSynced(measurement.id, true)
            }

            // 6. Sync Active Workout Plan
            val activePlan = planDao.getActivePlanWithDaysSync(actualUserId)
            if (activePlan != null) {
                saveWorkoutPlan(activePlan.toDomain())
            }

            // 7. Sync Daily Steps
            val unsyncedSteps = dailyStepsDao.getUnsyncedSteps()
            for (steps in unsyncedSteps) {
                saveDailySteps(steps)
                dailyStepsDao.markStepsSynced(steps.id, true)
            }

            // 8. Sync Outdoor Workouts
            val unsyncedWorkouts = outdoorWorkoutDao.getUnsyncedWorkouts()
            for (workout in unsyncedWorkouts) {
                saveOutdoorWorkout(workout)
                outdoorWorkoutDao.markWorkoutSynced(workout.id, true)
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    // 8. Bidirectional Down-Sync: Pull All User Data From Primary Cloud Firestore to Room SQLite
    suspend fun pullAllUserDataFromFirestore(userId: String): Boolean {
        if (userId.isBlank() || userId == "default_member") return false
        return try {
            // 1. User Profile
            val profile = getUserProfileFromFirestore(userId)
            if (profile != null) {
                userDao.upsertUser(profile)
            }

            // 2. Workout Sessions & Sets
            try {
                val sessionsSnapshot = firestore.collection(COLLECTION_SESSIONS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in sessionsSnapshot.documents) {
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    if (!isCompleted) continue

                    val sessionEntity = WorkoutSessionEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        planId = doc.getString("planId"),
                        workoutTitle = doc.getString("workoutTitle") ?: "Workout Session",
                        startTimeEpochMs = doc.getLong("startTimeEpochMs") ?: System.currentTimeMillis(),
                        endTimeEpochMs = doc.getLong("endTimeEpochMs"),
                        durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                        totalVolumeKg = (doc.getDouble("totalVolumeKg") ?: 0.0).toFloat(),
                        caloriesBurned = (doc.getLong("caloriesBurned") ?: 0L).toInt(),
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        setsCompleted = (doc.getLong("setsCompleted") ?: 0L).toInt(),
                        totalSets = (doc.getLong("totalSets") ?: 0L).toInt(),
                        isSynced = true
                    )
                    workoutSessionDao.upsertSession(sessionEntity)

                    val setsList = (doc.get("setLogs") as? List<*>)?.mapNotNull { setObj ->
                        val map = setObj as? Map<*, *> ?: return@mapNotNull null
                        SetLogEntity(
                            id = map["id"] as? String ?: UUID.randomUUID().toString(),
                            sessionId = map["sessionId"] as? String ?: sessionEntity.id,
                            exerciseId = map["exerciseId"] as? String ?: "",
                            exerciseName = map["exerciseName"] as? String ?: "",
                            setNumber = (map["setNumber"] as? Number)?.toInt() ?: 1,
                            targetReps = (map["targetReps"] as? Number)?.toInt() ?: 12,
                            targetWeightKg = (map["targetWeightKg"] as? Number)?.toFloat() ?: 20f,
                            actualReps = (map["actualReps"] as? Number)?.toInt() ?: 12,
                            actualWeightKg = (map["actualWeightKg"] as? Number)?.toFloat() ?: 20f,
                            completedAtEpochMs = (map["completedAtEpochMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            isSynced = true
                        )
                    } ?: emptyList()

                    for (setLog in setsList) {
                        workoutSessionDao.upsertSetLog(setLog)
                    }
                }
            } catch (_: Exception) {}

            // 3. Hydration Logs
            try {
                val hydrationSnapshot = firestore.collection(COLLECTION_HYDRATION_LOGS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in hydrationSnapshot.documents) {
                    val hydrationEntity = HydrationLogEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        amountMl = (doc.getLong("amountMl") ?: 0L).toInt(),
                        dailyGoalMl = (doc.getLong("dailyGoalMl") ?: 3000L).toInt(),
                        recordedAtEpochMs = doc.getLong("recordedAtEpochMs") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                    weightDao.upsertHydrationLog(hydrationEntity)
                }
            } catch (_: Exception) {}

            // 4. Weight Logs
            try {
                val weightSnapshot = firestore.collection(COLLECTION_WEIGHT_LOGS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in weightSnapshot.documents) {
                    val preWeight = (doc.getDouble("preWeightKg") ?: doc.getDouble("weightKg") ?: 0.0).toFloat()
                    val postWeight = (doc.getDouble("postWeightKg") ?: 0.0).toFloat()
                    val weightEntity = WeightLogEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        preWeightKg = preWeight,
                        postWeightKg = postWeight,
                        deltaKg = if (postWeight > 0f) postWeight - preWeight else 0f,
                        notes = doc.getString("notes") ?: doc.getString("note") ?: "",
                        recordedAtEpochMs = doc.getLong("recordedAtEpochMs") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                    weightDao.upsertWeightLog(weightEntity)
                }
            } catch (_: Exception) {}

            // 5. Body Measurements
            try {
                val measurementSnapshot = firestore.collection(COLLECTION_MEASUREMENTS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in measurementSnapshot.documents) {
                    val measurementEntity = BodyMeasurementEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        chestCm = (doc.getDouble("chestCm") ?: 0.0).toFloat(),
                        waistCm = (doc.getDouble("waistCm") ?: 0.0).toFloat(),
                        armsCm = (doc.getDouble("armsCm") ?: 0.0).toFloat(),
                        thighsCm = (doc.getDouble("thighsCm") ?: 0.0).toFloat(),
                        hipsCm = (doc.getDouble("hipsCm") ?: 0.0).toFloat(),
                        recordedAtEpochMs = doc.getLong("recordedAtEpochMs") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                    weightDao.upsertMeasurement(measurementEntity)
                }
            } catch (_: Exception) {}

            // 6. Active Workout Plan
            try {
                val planSnapshot = firestore.collection(COLLECTION_PLANS)
                    .whereEqualTo("memberId", userId)
                    .whereEqualTo("isActive", true)
                    .limit(1)
                    .get()
                    .await()

                val planDoc = planSnapshot.documents.firstOrNull()
                if (planDoc != null && planDoc.exists()) {
                    val daysList = (planDoc.get("days") as? List<*>)?.mapNotNull { dayObj ->
                        val dayMap = dayObj as? Map<*, *> ?: return@mapNotNull null
                        val exList = (dayMap["exercises"] as? List<*>)?.mapNotNull { exObj ->
                            val exMap = exObj as? Map<*, *> ?: return@mapNotNull null
                            WorkoutExercise(
                                exerciseId = exMap["exerciseId"] as? String ?: "",
                                exerciseName = exMap["exerciseName"] as? String ?: "",
                                category = exMap["category"] as? String ?: "",
                                targetSets = (exMap["targetSets"] as? Number)?.toInt() ?: 3,
                                targetReps = (exMap["targetReps"] as? Number)?.toInt() ?: 12,
                                targetWeightKg = (exMap["targetWeightKg"] as? Number)?.toFloat() ?: 20f,
                                restDurationSeconds = (exMap["restDurationSeconds"] as? Number)?.toInt() ?: 45,
                                notes = exMap["notes"] as? String ?: ""
                            )
                        } ?: emptyList()

                        WorkoutDay(
                            dayOfWeek = dayMap["dayOfWeek"] as? String ?: "Monday",
                            title = dayMap["title"] as? String ?: "Workout",
                            isCompleted = dayMap["isCompleted"] as? Boolean ?: false,
                            isRestDay = dayMap["isRestDay"] as? Boolean ?: false,
                            exercises = exList
                        )
                    } ?: emptyList()

                    val parsedPlan = WorkoutPlan(
                        id = planDoc.getString("id") ?: planDoc.id,
                        trainerId = planDoc.getString("trainerId") ?: "",
                        memberId = planDoc.getString("memberId") ?: userId,
                        name = planDoc.getString("name") ?: "Custom Workout Plan",
                        version = planDoc.getLong("version")?.toInt() ?: 1,
                        isActive = planDoc.getBoolean("isActive") ?: true,
                        days = daysList
                    )
                    planDao.replaceCompletePlan(parsedPlan)
                } else {
                    // Check if user is a coach with plans created
                    val trainerPlans = firestore.collection(COLLECTION_PLANS)
                        .whereEqualTo("trainerId", userId)
                        .get()
                        .await()
                    for (doc in trainerPlans.documents) {
                        val daysList = (doc.get("days") as? List<*>)?.mapNotNull { dayObj ->
                            val dayMap = dayObj as? Map<*, *> ?: return@mapNotNull null
                            val exList = (dayMap["exercises"] as? List<*>)?.mapNotNull { exObj ->
                                val exMap = exObj as? Map<*, *> ?: return@mapNotNull null
                                WorkoutExercise(
                                    exerciseId = exMap["exerciseId"] as? String ?: "",
                                    exerciseName = exMap["exerciseName"] as? String ?: "",
                                    category = exMap["category"] as? String ?: "",
                                    targetSets = (exMap["targetSets"] as? Number)?.toInt() ?: 3,
                                    targetReps = (exMap["targetReps"] as? Number)?.toInt() ?: 12,
                                    targetWeightKg = (exMap["targetWeightKg"] as? Number)?.toFloat() ?: 20f,
                                    restDurationSeconds = (exMap["restDurationSeconds"] as? Number)?.toInt() ?: 45,
                                    notes = exMap["notes"] as? String ?: ""
                                )
                            } ?: emptyList()

                            WorkoutDay(
                                dayOfWeek = dayMap["dayOfWeek"] as? String ?: "Monday",
                                title = dayMap["title"] as? String ?: "Workout",
                                isCompleted = dayMap["isCompleted"] as? Boolean ?: false,
                                isRestDay = dayMap["isRestDay"] as? Boolean ?: false,
                                exercises = exList
                            )
                        } ?: emptyList()

                        val parsedPlan = WorkoutPlan(
                            id = doc.getString("id") ?: doc.id,
                            trainerId = doc.getString("trainerId") ?: userId,
                            memberId = doc.getString("memberId") ?: "",
                            name = doc.getString("name") ?: "Workout Plan",
                            version = doc.getLong("version")?.toInt() ?: 1,
                            isActive = doc.getBoolean("isActive") ?: true,
                            days = daysList
                        )
                        planDao.replaceCompletePlan(parsedPlan)
                    }
                }
            } catch (_: Exception) {}

            // 7. Daily Steps
            try {
                val stepsSnapshot = firestore.collection(COLLECTION_DAILY_STEPS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in stepsSnapshot.documents) {
                    val stepsEntity = DailyStepsEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        date = doc.getString("date") ?: "",
                        stepCount = (doc.getLong("stepCount") ?: 0L).toInt(),
                        targetGoal = (doc.getLong("targetGoal") ?: 10000L).toInt(),
                        distanceMeters = (doc.getDouble("distanceMeters") ?: 0.0).toFloat(),
                        caloriesKcal = (doc.getLong("caloriesKcal") ?: 0L).toInt(),
                        activeMinutes = (doc.getLong("activeMinutes") ?: 0L).toInt(),
                        lastSensorStepCount = (doc.getLong("lastSensorStepCount") ?: 0L).toInt(),
                        lastUpdatedEpochMs = doc.getLong("lastUpdatedEpochMs") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                    dailyStepsDao.upsertDailySteps(stepsEntity)
                }
            } catch (_: Exception) {}

            // 8. Outdoor Workouts
            try {
                val outdoorSnapshot = firestore.collection(COLLECTION_OUTDOOR_WORKOUTS)
                    .whereEqualTo("memberId", userId)
                    .get()
                    .await()

                for (doc in outdoorSnapshot.documents) {
                    val outdoorEntity = OutdoorWorkoutEntity(
                        id = doc.getString("id") ?: doc.id,
                        memberId = doc.getString("memberId") ?: userId,
                        workoutType = doc.getString("workoutType") ?: "RUN",
                        startTimeEpochMs = doc.getLong("startTimeEpochMs") ?: System.currentTimeMillis(),
                        endTimeEpochMs = doc.getLong("endTimeEpochMs"),
                        durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                        totalSteps = (doc.getLong("totalSteps") ?: 0L).toInt(),
                        totalDistanceMeters = (doc.getDouble("totalDistanceMeters") ?: 0.0).toFloat(),
                        avgPaceSecondsPerKm = (doc.getDouble("avgPaceSecondsPerKm") ?: 0.0).toFloat(),
                        maxSpeedMps = (doc.getDouble("maxSpeedMps") ?: 0.0).toFloat(),
                        caloriesBurned = (doc.getLong("caloriesBurned") ?: 0L).toInt(),
                        polylineEncoded = doc.getString("polylineEncoded") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: true,
                        isSynced = true
                    )
                    outdoorWorkoutDao.upsertOutdoorWorkout(outdoorEntity)
                }
            } catch (_: Exception) {}

            // 9. Connected Members (If Trainer)
            try {
                pullConnectedMembersForTrainer(userId)
            } catch (_: Exception) {}

            // 10. Member's Own Membership Details (If Member)
            try {
                pullMemberMembershipDetails(userId)
            } catch (_: Exception) {}

            true
        } catch (_: Exception) {
            false
        }
    }

    // 9. Link Member to Trainer via Invite Code
    suspend fun linkTrainerByCode(code: String, memberId: String): String? {
        val codeDoc = firestore.collection(COLLECTION_TRAINER_CODES)
            .document(code.trim().uppercase())
            .get()
            .await()

        if (!codeDoc.exists()) return null

        val trainerId = codeDoc.getString("trainerId") ?: return null

        // Atomically append memberId to trainer's activeMemberIds array
        firestore.collection(COLLECTION_TRAINERS)
            .document(trainerId)
            .update("activeMemberIds", FieldValue.arrayUnion(memberId))
            .await()

        return trainerId
    }

    // 10. Publish Trainer Invite Code to Cloud Firestore
    suspend fun publishTrainerInviteCode(
        code: String,
        trainerId: String,
        trainerName: String,
        gymName: String = "GymFlow Fitness Center"
    ): Boolean {
        return try {
            val cleanCode = code.trim().uppercase()
            val codeData = mapOf(
                "code" to cleanCode,
                "trainerId" to trainerId,
                "trainerName" to trainerName,
                "gymName" to gymName,
                "createdAtEpochMs" to System.currentTimeMillis(),
                "expiresAtEpochMs" to System.currentTimeMillis() + (24L * 60 * 60 * 1000)
            )
            firestore.collection(COLLECTION_TRAINER_CODES)
                .document(cleanCode)
                .set(codeData, SetOptions.merge())
                .await()

            val trainerData = mapOf(
                "id" to trainerId,
                "name" to trainerName,
                "gymName" to gymName,
                "currentInviteCode" to cleanCode,
                "lastUpdated" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .set(trainerData, SetOptions.merge())
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // 10b. Query and Resolve Trainer Invite Code from Cloud Firestore
    suspend fun lookupTrainerInviteCode(code: String): CloudTrainerCodeInfo? {
        return try {
            val cleanCode = code.trim().uppercase()
            val doc = firestore.collection(COLLECTION_TRAINER_CODES)
                .document(cleanCode)
                .get()
                .await()
            if (doc.exists()) {
                val trainerId = doc.getString("trainerId") ?: ""
                val trainerName = doc.getString("trainerName") ?: "Coach"
                val gymName = doc.getString("gymName") ?: "GymFlow Fitness Center"
                val expiresAt = doc.getLong("expiresAtEpochMs") ?: (System.currentTimeMillis() + (24L * 60 * 60 * 1000))
                CloudTrainerCodeInfo(
                    code = cleanCode,
                    trainerId = trainerId,
                    trainerName = trainerName,
                    gymName = gymName,
                    expiresAtEpochMs = expiresAt
                )
            } else {
                val query = firestore.collection(COLLECTION_TRAINERS)
                    .whereEqualTo("currentInviteCode", cleanCode)
                    .limit(1)
                    .get()
                    .await()
                val trainerDoc = query.documents.firstOrNull()
                if (trainerDoc != null && trainerDoc.exists()) {
                    CloudTrainerCodeInfo(
                        code = cleanCode,
                        trainerId = trainerDoc.getString("id") ?: trainerDoc.id,
                        trainerName = trainerDoc.getString("name") ?: "Coach",
                        gymName = trainerDoc.getString("gymName") ?: "GymFlow Fitness Center",
                        expiresAtEpochMs = System.currentTimeMillis() + (24L * 60 * 60 * 1000)
                    )
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    // 10c. Get Trainer Profile and Invite Code info for members
    suspend fun getTrainerProfileInfo(trainerId: String): CloudTrainerInfo? {
        if (trainerId.isBlank() || trainerId == "default_member") return null
        return try {
            val trainerDoc = firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .get()
                .await()
            if (trainerDoc.exists()) {
                val name = trainerDoc.getString("name") ?: "Coach"
                val gymName = trainerDoc.getString("gymName") ?: "GymFlow Fitness Center"
                val code = trainerDoc.getString("currentInviteCode") ?: ""
                val upi = trainerDoc.getString("upiId") ?: ""
                CloudTrainerInfo(trainerId, name, gymName, code, upi)
            } else {
                val userDoc = firestore.collection(COLLECTION_USERS)
                    .document(trainerId)
                    .get()
                    .await()
                if (userDoc.exists()) {
                    val name = userDoc.getString("name") ?: "Coach"
                    val upi = userDoc.getString("upiId") ?: ""
                    CloudTrainerInfo(trainerId, name, "GymFlow Fitness Center", "", upi)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    // 11. Pull Connected Members for Trainer and upsert into local Room database
    suspend fun pullConnectedMembersForTrainer(trainerId: String): List<GymMemberEntity> {
        if (trainerId.isBlank() || trainerId == "default_member") return emptyList()
        return try {
            val membersList = mutableListOf<GymMemberEntity>()
            val seenMemberIds = mutableSetOf<String>()

            // A. Fetch from trainers/{trainerId}/connected_members
            try {
                val subSnapshot = firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .collection("connected_members")
                    .get()
                    .await()

                for (doc in subSnapshot.documents) {
                    val mId = doc.getString("id") ?: doc.id
                    if (mId.isBlank() || mId in seenMemberIds) continue
                    seenMemberIds.add(mId)
                    val mName = doc.getString("name") ?: "Athlete"
                    val mEmail = doc.getString("email") ?: ""
                    val mPhone = doc.getString("phoneNumber") ?: ""
                    val joinedAt = doc.getLong("joinedAtEpochMs") ?: System.currentTimeMillis()
                    val code = doc.getString("invitationCode") ?: ""

                    var height = (doc.getDouble("heightCm") ?: 0.0).toFloat()
                    var weight = (doc.getDouble("currentWeightKg") ?: 0.0).toFloat()
                    var goal = doc.getString("fitnessGoal") ?: "General Fitness"
                    var age = (doc.getLong("age") ?: 0L).toInt()

                    try {
                        val uDoc = firestore.collection(COLLECTION_USERS).document(mId).get().await()
                        if (uDoc.exists()) {
                            if (height == 0f) height = (uDoc.getDouble("heightCm") ?: 0.0).toFloat()
                            if (weight == 0f) weight = (uDoc.getDouble("currentWeightKg") ?: 0.0).toFloat()
                            if (goal == "General Fitness") goal = uDoc.getString("fitnessGoal") ?: goal
                            if (age == 0) age = (uDoc.getLong("age") ?: 0L).toInt()
                        }
                    } catch (_: Exception) {}

                    val userEntity = UserEntity(
                        id = mId,
                        name = mName,
                        email = mEmail,
                        role = Role.MEMBER,
                        heightCm = height,
                        currentWeightKg = weight,
                        fitnessGoal = goal,
                        age = age,
                        connectedTrainerId = trainerId,
                        syncStatus = "Connected",
                        lastUpdated = System.currentTimeMillis()
                    )
                    userDao.upsertUser(userEntity)

                    val gymMemberEntity = GymMemberEntity(
                        id = mId,
                        name = mName,
                        phoneNumber = mPhone,
                        email = mEmail,
                        assignedTrainerId = trainerId,
                        assignedTrainerName = "Coach",
                        durationMonths = 1,
                        startDateEpochMs = joinedAt,
                        endDateEpochMs = joinedAt + (30L * 24 * 60 * 60 * 1000),
                        feeAmount = 1500.0,
                        amountPaid = 1500.0,
                        paymentStatus = "PAID",
                        paymentMethod = "UPI",
                        notes = if (code.isNotBlank()) "Joined via Code: $code" else "Connected athlete",
                        isActive = true
                    )
                    gymMemberDao.insertMember(gymMemberEntity)
                    membersList.add(gymMemberEntity)
                }
            } catch (_: Exception) {}

            // B. Fetch from users collection where connectedTrainerId == trainerId
            try {
                val usersSnapshot = firestore.collection(COLLECTION_USERS)
                    .whereEqualTo("connectedTrainerId", trainerId)
                    .get()
                    .await()

                for (doc in usersSnapshot.documents) {
                    val mId = doc.getString("id") ?: doc.id
                    if (mId.isBlank() || mId in seenMemberIds) continue
                    seenMemberIds.add(mId)
                    val mName = doc.getString("name") ?: "Athlete"
                    val mEmail = doc.getString("email") ?: ""
                    val mPhone = doc.getString("phoneNumber") ?: ""
                    val height = (doc.getDouble("heightCm") ?: 0.0).toFloat()
                    val weight = (doc.getDouble("currentWeightKg") ?: 0.0).toFloat()
                    val goal = doc.getString("fitnessGoal") ?: "General Fitness"
                    val age = (doc.getLong("age") ?: 0L).toInt()

                    val userEntity = UserEntity(
                        id = mId,
                        name = mName,
                        email = mEmail,
                        role = Role.MEMBER,
                        heightCm = height,
                        currentWeightKg = weight,
                        fitnessGoal = goal,
                        age = age,
                        connectedTrainerId = trainerId,
                        syncStatus = "Connected",
                        lastUpdated = System.currentTimeMillis()
                    )
                    userDao.upsertUser(userEntity)

                    val gymMemberEntity = GymMemberEntity(
                        id = mId,
                        name = mName,
                        phoneNumber = mPhone,
                        email = mEmail,
                        assignedTrainerId = trainerId,
                        assignedTrainerName = "Coach",
                        durationMonths = 1,
                        startDateEpochMs = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis(),
                        endDateEpochMs = (doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()) + (30L * 24 * 60 * 60 * 1000),
                        feeAmount = 1500.0,
                        amountPaid = 1500.0,
                        paymentStatus = "PAID",
                        paymentMethod = "UPI",
                        notes = "Connected athlete",
                        isActive = true
                    )
                    gymMemberDao.insertMember(gymMemberEntity)
                    membersList.add(gymMemberEntity)
                }
            } catch (_: Exception) {}

            // C. Fetch from trainers/{trainerId}.activeMemberIds
            try {
                val trainerDoc = firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .get()
                    .await()
                val activeIds = (trainerDoc.get("activeMemberIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                for (mId in activeIds) {
                    if (mId.isBlank() || mId in seenMemberIds) continue
                    seenMemberIds.add(mId)
                    val uDoc = firestore.collection(COLLECTION_USERS).document(mId).get().await()
                    if (uDoc.exists()) {
                        val mName = uDoc.getString("name") ?: "Athlete"
                        val mEmail = uDoc.getString("email") ?: ""
                        val mPhone = uDoc.getString("phoneNumber") ?: ""
                        val height = (uDoc.getDouble("heightCm") ?: 0.0).toFloat()
                        val weight = (uDoc.getDouble("currentWeightKg") ?: 0.0).toFloat()
                        val goal = uDoc.getString("fitnessGoal") ?: "General Fitness"
                        val age = (uDoc.getLong("age") ?: 0L).toInt()

                        val userEntity = UserEntity(
                            id = mId,
                            name = mName,
                            email = mEmail,
                            role = Role.MEMBER,
                            heightCm = height,
                            currentWeightKg = weight,
                            fitnessGoal = goal,
                            age = age,
                            connectedTrainerId = trainerId,
                            syncStatus = "Connected",
                            lastUpdated = System.currentTimeMillis()
                        )
                        userDao.upsertUser(userEntity)

                        val gymMemberEntity = GymMemberEntity(
                            id = mId,
                            name = mName,
                            phoneNumber = mPhone,
                            email = mEmail,
                            assignedTrainerId = trainerId,
                            assignedTrainerName = "Coach",
                            durationMonths = 1,
                            startDateEpochMs = uDoc.getLong("createdAtEpochMs") ?: System.currentTimeMillis(),
                            endDateEpochMs = (uDoc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()) + (30L * 24 * 60 * 60 * 1000),
                            feeAmount = 1500.0,
                            amountPaid = 1500.0,
                            paymentStatus = "PAID",
                            paymentMethod = "UPI",
                            notes = "Connected athlete",
                            isActive = true
                        )
                        gymMemberDao.insertMember(gymMemberEntity)
                        membersList.add(gymMemberEntity)
                    }
                }
            } catch (_: Exception) {}

            membersList
        } catch (_: Exception) {
            emptyList()
        }
    }

    // 12. Link Member to Trainer in Cloud Firestore (Two-way Connection)
    suspend fun registerMemberTrainerConnection(
        code: String,
        memberId: String,
        memberName: String,
        memberEmail: String,
        trainerId: String,
        trainerName: String,
        memberPhone: String = ""
    ): Boolean {
        return try {
            val cleanCode = code.trim().uppercase()
            val now = System.currentTimeMillis()

            // 1) Update member's user profile in Firestore
            val memberProfileUpdate = mutableMapOf<String, Any>(
                "connectedTrainerId" to trainerId,
                "connectedTrainerName" to trainerName,
                "syncStatus" to "Connected",
                "lastUpdated" to now
            )
            if (memberPhone.isNotBlank()) {
                memberProfileUpdate["phoneNumber"] = memberPhone
            }
            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .set(memberProfileUpdate, SetOptions.merge())
                .await()

            // 2) Append to trainer's connected_members subcollection
            val connectedMemberData = mutableMapOf<String, Any>(
                "id" to memberId,
                "name" to memberName,
                "email" to memberEmail,
                "phoneNumber" to memberPhone,
                "connectedTrainerId" to trainerId,
                "connectedTrainerName" to trainerName,
                "joinedAtEpochMs" to now,
                "invitationCode" to cleanCode
            )
            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .collection("connected_members")
                .document(memberId)
                .set(connectedMemberData, SetOptions.merge())
                .await()

            // 3) Append memberId to trainer's activeMemberIds array
            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .update("activeMemberIds", FieldValue.arrayUnion(memberId))
                .await()

            true
        } catch (_: Exception) {
            false
        }
    }

    // 13. Update Member Phone Number in Firestore
    suspend fun updateMemberPhoneNumber(
        memberId: String,
        phoneNumber: String,
        trainerId: String? = null
    ): Boolean {
        return try {
            val cleanPhone = phoneNumber.trim()
            val now = System.currentTimeMillis()

            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .set(mapOf("phoneNumber" to cleanPhone, "lastUpdated" to now), SetOptions.merge())
                .await()

            val targetTrainerId = trainerId ?: run {
                val uDoc = firestore.collection(COLLECTION_USERS).document(memberId).get().await()
                uDoc.getString("connectedTrainerId")
            }

            if (!targetTrainerId.isNullOrBlank()) {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(targetTrainerId)
                    .collection("connected_members")
                    .document(memberId)
                    .set(mapOf("phoneNumber" to cleanPhone), SetOptions.merge())
                    .await()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // 14. Remove Member Trainer Connection in Cloud Firestore
    suspend fun removeMemberTrainerConnection(
        memberId: String,
        trainerId: String
    ): Boolean {
        return try {
            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .set(
                    mapOf(
                        "connectedTrainerId" to FieldValue.delete(),
                        "connectedTrainerName" to FieldValue.delete(),
                        "syncStatus" to "Not Connected",
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()

            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .collection("connected_members")
                .document(memberId)
                .delete()
                .await()

            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .update("activeMemberIds", FieldValue.arrayRemove(memberId))
                .await()

            true
        } catch (_: Exception) {
            false
        }
    }

    // 15. Update Trainer Gym Name across Cloud Firestore
    suspend fun updateTrainerGymName(trainerId: String, newGymName: String): Boolean {
        if (trainerId.isBlank() || trainerId == "default_member") return false
        return try {
            val cleanName = newGymName.trim().ifBlank { "GymFlow Fitness Center" }
            val now = System.currentTimeMillis()

            // A. Update trainers/{trainerId}.gymName
            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .set(mapOf("gymName" to cleanName, "lastUpdated" to now), SetOptions.merge())
                .await()

            // B. Update trainer's invite codes
            try {
                val codesSnapshot = firestore.collection(COLLECTION_TRAINER_CODES)
                    .whereEqualTo("trainerId", trainerId)
                    .get()
                    .await()
                for (doc in codesSnapshot.documents) {
                    firestore.collection(COLLECTION_TRAINER_CODES)
                        .document(doc.id)
                        .set(mapOf("gymName" to cleanName), SetOptions.merge())
                        .await()
                }
            } catch (_: Exception) {}

            // C. Update connected members subcollection
            try {
                val membersSnapshot = firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .collection("connected_members")
                    .get()
                    .await()
                for (doc in membersSnapshot.documents) {
                    firestore.collection(COLLECTION_TRAINERS)
                        .document(trainerId)
                        .collection("connected_members")
                        .document(doc.id)
                        .set(mapOf("gymName" to cleanName), SetOptions.merge())
                        .await()
                }
            } catch (_: Exception) {}

            true
        } catch (_: Exception) {
            false
        }
    }

    // 16. Renew Member Membership in Cloud Firestore
    suspend fun renewMemberMembership(
        memberId: String,
        durationMonths: Int,
        feeAmount: Double,
        trainerId: String? = null
    ): Boolean {
        if (memberId.isBlank() || memberId == "default_member") return false
        return try {
            val now = System.currentTimeMillis()
            val extensionMs = durationMonths * 30L * 24 * 60 * 60 * 1000

            var newEndDate = now + extensionMs
            try {
                val uDoc = firestore.collection(COLLECTION_USERS).document(memberId).get().await()
                val currentEnd = (uDoc.get("membership") as? Map<*, *>)?.get("endDateEpochMs") as? Long ?: 0L
                if (currentEnd > now) {
                    newEndDate = currentEnd + extensionMs
                }
            } catch (_: Exception) {}

            val membershipData = mapOf(
                "durationMonths" to durationMonths,
                "startDateEpochMs" to now,
                "endDateEpochMs" to newEndDate,
                "paymentStatus" to "PAID",
                "paymentMethod" to "UPI",
                "feeAmount" to feeAmount,
                "lastRenewedEpochMs" to now
            )

            // Update user document
            firestore.collection(COLLECTION_USERS)
                .document(memberId)
                .set(mapOf("membership" to membershipData, "lastUpdated" to now), SetOptions.merge())
                .await()

            // Update trainer subcollection if connected
            val targetTrainer = trainerId ?: run {
                val uDoc = firestore.collection(COLLECTION_USERS).document(memberId).get().await()
                uDoc.getString("connectedTrainerId")
            }

            if (!targetTrainer.isNullOrBlank()) {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(targetTrainer)
                    .collection("connected_members")
                    .document(memberId)
                    .set(mapOf(
                        "endDateEpochMs" to newEndDate,
                        "durationMonths" to durationMonths,
                        "paymentStatus" to "PAID",
                        "feeAmount" to feeAmount,
                        "lastRenewedEpochMs" to now
                    ), SetOptions.merge())
                    .await()
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    // 17. Pull Member's own Membership Details from Cloud Firestore
    suspend fun pullMemberMembershipDetails(userId: String): GymMemberEntity? {
        if (userId.isBlank() || userId == "default_member") return null
        return try {
            val uDoc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (!uDoc.exists()) return null

            val trainerId = uDoc.getString("connectedTrainerId")
            val name = uDoc.getString("name") ?: "Athlete"
            val email = uDoc.getString("email") ?: ""
            val phone = uDoc.getString("phoneNumber") ?: ""

            var startDate = uDoc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
            var endDate = startDate + (30L * 24 * 60 * 60 * 1000)
            var durationMonths = 1
            var feeAmount = 1500.0
            var paymentStatus = "PAID"
            var trainerName = uDoc.getString("connectedTrainerName") ?: "Coach"
            var gymName = "GymFlow Fitness Center"

            if (!trainerId.isNullOrBlank()) {
                try {
                    val trainerDoc = firestore.collection(COLLECTION_TRAINERS).document(trainerId).get().await()
                    if (trainerDoc.exists()) {
                        gymName = trainerDoc.getString("gymName") ?: gymName
                        trainerName = trainerDoc.getString("name") ?: trainerName
                    }
                    val cmDoc = firestore.collection(COLLECTION_TRAINERS)
                        .document(trainerId)
                        .collection("connected_members")
                        .document(userId)
                        .get()
                        .await()
                    if (cmDoc.exists()) {
                        startDate = cmDoc.getLong("joinedAtEpochMs") ?: startDate
                        endDate = cmDoc.getLong("endDateEpochMs") ?: (startDate + (30L * 24 * 60 * 60 * 1000))
                        durationMonths = cmDoc.getLong("durationMonths")?.toInt() ?: 1
                        feeAmount = cmDoc.getDouble("feeAmount") ?: 1500.0
                        paymentStatus = cmDoc.getString("paymentStatus") ?: "PAID"
                    }
                } catch (_: Exception) {}
            }

            val membershipMap = uDoc.get("membership") as? Map<*, *>
            if (membershipMap != null) {
                startDate = (membershipMap["startDateEpochMs"] as? Number)?.toLong() ?: startDate
                endDate = (membershipMap["endDateEpochMs"] as? Number)?.toLong() ?: endDate
                durationMonths = (membershipMap["durationMonths"] as? Number)?.toInt() ?: durationMonths
                feeAmount = (membershipMap["feeAmount"] as? Number)?.toDouble() ?: feeAmount
                paymentStatus = (membershipMap["paymentStatus"] as? String) ?: paymentStatus
            }

            val memberEntity = GymMemberEntity(
                id = userId,
                name = name,
                phoneNumber = phone,
                email = email,
                assignedTrainerId = trainerId,
                assignedTrainerName = trainerName,
                durationMonths = durationMonths,
                startDateEpochMs = startDate,
                endDateEpochMs = endDate,
                feeAmount = feeAmount,
                amountPaid = feeAmount,
                paymentStatus = paymentStatus,
                paymentMethod = "UPI",
                notes = "Gym Membership: $gymName",
                isActive = true
            )
            gymMemberDao.insertMember(memberEntity)
            memberEntity
        } catch (_: Exception) {
            null
        }
    }

    // 18. Update Trainer UPI ID & Business Name
    suspend fun updateTrainerUpi(trainerId: String, upiId: String, upiName: String): Boolean {
        if (trainerId.isBlank() || trainerId == "default_member") return false
        return try {
            val cleanUpi = upiId.trim()
            val cleanName = upiName.trim()
            val now = System.currentTimeMillis()

            val upiData = mapOf(
                "upiId" to cleanUpi,
                "upiAccountName" to cleanName,
                "lastUpdated" to now
            )

            // Update trainers/{trainerId}
            firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .set(upiData, SetOptions.merge())
                .await()

            // Update users/{trainerId}
            firestore.collection(COLLECTION_USERS)
                .document(trainerId)
                .set(upiData, SetOptions.merge())
                .await()

            true
        } catch (_: Exception) {
            false
        }
    }

    // 19. Create Membership Renewal Order in Cloud Firestore
    suspend fun createMembershipOrder(order: MembershipOrder): Boolean {
        return try {
            val orderMap = mapOf(
                "orderId" to order.orderId,
                "memberId" to order.memberId,
                "memberName" to order.memberName,
                "memberEmail" to order.memberEmail,
                "memberPhone" to order.memberPhone,
                "trainerId" to order.trainerId,
                "trainerName" to order.trainerName,
                "gymName" to order.gymName,
                "planDurationMonths" to order.planDurationMonths,
                "planDurationDisplayName" to order.planDurationDisplayName,
                "amountInr" to order.amountInr,
                "paymentMethod" to order.paymentMethod,
                "targetUpiId" to order.targetUpiId,
                "utrNumber" to (order.utrNumber ?: ""),
                "orderStatus" to order.orderStatus.name,
                "createdAtEpochMs" to order.createdAtEpochMs,
                "approvedAtEpochMs" to (order.approvedAtEpochMs ?: 0L),
                "notes" to order.notes
            )

            // A. Global membership_orders collection
            firestore.collection(COLLECTION_MEMBERSHIP_ORDERS)
                .document(order.orderId)
                .set(orderMap, SetOptions.merge())
                .await()

            // B. Trainer's private subcollection trainers/{trainerId}/renewal_orders
            if (order.trainerId.isNotBlank() && order.trainerId != "default_member") {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(order.trainerId)
                    .collection("renewal_orders")
                    .document(order.orderId)
                    .set(orderMap, SetOptions.merge())
                    .await()
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    // 20. Submit Order Payment Proof (UTR Number)
    suspend fun submitOrderPaymentProof(orderId: String, utrNumber: String, trainerId: String): Boolean {
        return try {
            val cleanUtr = utrNumber.trim()
            val now = System.currentTimeMillis()
            val update = mapOf(
                "utrNumber" to cleanUtr,
                "orderStatus" to OrderStatus.PENDING_VERIFICATION.name,
                "submittedAtEpochMs" to now
            )

            firestore.collection(COLLECTION_MEMBERSHIP_ORDERS)
                .document(orderId)
                .set(update, SetOptions.merge())
                .await()

            if (trainerId.isNotBlank() && trainerId != "default_member") {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .collection("renewal_orders")
                    .document(orderId)
                    .set(update, SetOptions.merge())
                    .await()
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    // 21. Pull Pending Renewal Orders for Trainer
    suspend fun pullPendingOrdersForTrainer(trainerId: String): List<MembershipOrder> {
        if (trainerId.isBlank() || trainerId == "default_member") return emptyList()
        return try {
            val orders = mutableListOf<MembershipOrder>()
            val snapshot = firestore.collection(COLLECTION_TRAINERS)
                .document(trainerId)
                .collection("renewal_orders")
                .whereIn("orderStatus", listOf(OrderStatus.PENDING_VERIFICATION.name, OrderStatus.PENDING.name))
                .get()
                .await()

            for (doc in snapshot.documents) {
                val oId = doc.getString("orderId") ?: doc.id
                val mId = doc.getString("memberId") ?: ""
                val mName = doc.getString("memberName") ?: "Member"
                val mEmail = doc.getString("memberEmail") ?: ""
                val mPhone = doc.getString("memberPhone") ?: ""
                val tName = doc.getString("trainerName") ?: "Coach"
                val gName = doc.getString("gymName") ?: "GymFlow Fitness Center"
                val duration = doc.getLong("planDurationMonths")?.toInt() ?: 1
                val durationLabel = doc.getString("planDurationDisplayName") ?: "$duration Month Pass"
                val amount = doc.getDouble("amountInr") ?: 1500.0
                val method = doc.getString("paymentMethod") ?: "UPI"
                val upi = doc.getString("targetUpiId") ?: ""
                val utr = doc.getString("utrNumber")
                val statusStr = doc.getString("orderStatus")
                val created = doc.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
                val notes = doc.getString("notes") ?: ""

                orders.add(
                    MembershipOrder(
                        orderId = oId,
                        memberId = mId,
                        memberName = mName,
                        memberEmail = mEmail,
                        memberPhone = mPhone,
                        trainerId = trainerId,
                        trainerName = tName,
                        gymName = gName,
                        planDurationMonths = duration,
                        planDurationDisplayName = durationLabel,
                        amountInr = amount,
                        paymentMethod = method,
                        targetUpiId = upi,
                        utrNumber = utr,
                        orderStatus = OrderStatus.fromString(statusStr),
                        createdAtEpochMs = created,
                        notes = notes
                    )
                )
            }
            orders.sortedByDescending { it.createdAtEpochMs }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // 22. Approve Membership Order & Extend Member's Pass
    suspend fun approveMembershipOrder(
        orderId: String,
        trainerId: String,
        memberId: String,
        durationMonths: Int,
        feeAmount: Double
    ): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val update = mapOf(
                "orderStatus" to OrderStatus.APPROVED.name,
                "approvedAtEpochMs" to now
            )

            // Update order status
            firestore.collection(COLLECTION_MEMBERSHIP_ORDERS)
                .document(orderId)
                .set(update, SetOptions.merge())
                .await()

            if (trainerId.isNotBlank() && trainerId != "default_member") {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .collection("renewal_orders")
                    .document(orderId)
                    .set(update, SetOptions.merge())
                    .await()
            }

            // Immediately extend the member's pass
            renewMemberMembership(
                memberId = memberId,
                durationMonths = durationMonths,
                feeAmount = feeAmount,
                trainerId = trainerId
            )

            true
        } catch (_: Exception) {
            false
        }
    }

    // 23. Reject Membership Order
    suspend fun rejectMembershipOrder(orderId: String, trainerId: String, reason: String = "Invalid UTR / Payment not received"): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val update = mapOf(
                "orderStatus" to OrderStatus.REJECTED.name,
                "rejectedAtEpochMs" to now,
                "rejectionReason" to reason
            )

            firestore.collection(COLLECTION_MEMBERSHIP_ORDERS)
                .document(orderId)
                .set(update, SetOptions.merge())
                .await()

            if (trainerId.isNotBlank() && trainerId != "default_member") {
                firestore.collection(COLLECTION_TRAINERS)
                    .document(trainerId)
                    .collection("renewal_orders")
                    .document(orderId)
                    .set(update, SetOptions.merge())
                    .await()
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
