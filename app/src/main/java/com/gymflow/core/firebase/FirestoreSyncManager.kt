package com.gymflow.core.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.domain.model.ExerciseSet
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

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightDao: WeightDao,
    private val planDao: PlanDao
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
                }
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
}
