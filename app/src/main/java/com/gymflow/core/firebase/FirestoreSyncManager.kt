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
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(COLLECTION_USERS)
            .document(user.id)
            .set(userMap, SetOptions.merge())
            .await()
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

    // 8. Link Member to Trainer via Invite Code
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
