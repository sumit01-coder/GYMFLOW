package com.gymflow.core.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.gymflow.core.database.BodyMeasurementEntity
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
    private val firestore: FirebaseFirestore
) {

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_TRAINERS = "trainers"
        const val COLLECTION_TRAINER_CODES = "trainer_invite_codes"
        const val COLLECTION_PLANS = "plans"
        const val COLLECTION_SESSIONS = "workout_sessions"
        const val COLLECTION_MEASUREMENTS = "body_measurements"
    }

    // 1. Sync & Observe Workout Plans in Real-Time
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

    // 2. Sync Workout Sessions
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

    // 3. Sync Body Measurements
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

    // 4. Link Member to Trainer via Invite Code
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
