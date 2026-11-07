package com.gymflow.core.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.DailyStepsDao
import com.gymflow.core.database.DailyStepsEntity
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
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class FirestoreConnectionStatus {
    CONNECTED,    // Online and receiving live cloud snapshots
    SYNCING,      // Pushing/pulling changes or pending writes in progress
    OFFLINE       // Operating in local persistent disk cache mode
}

/**
 * Enterprise Connection Engine for Cloud Firestore.
 * Maintains persistent, real-time snapshot listeners for authenticated users,
 * reacts to network connectivity shifts, and ensures bidirectional consistency
 * between Room SQLite and Cloud Firestore.
 */
@Singleton
class FirestoreConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightDao: WeightDao,
    private val planDao: PlanDao,
    private val dailyStepsDao: DailyStepsDao,
    private val outdoorWorkoutDao: OutdoorWorkoutDao
) {
    private val tag = "FirestoreConnectionMgr"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionStatus = MutableStateFlow(FirestoreConnectionStatus.OFFLINE)
    val connectionStatus: StateFlow<FirestoreConnectionStatus> = _connectionStatus.asStateFlow()

    private val activeRegistrations = mutableListOf<ListenerRegistration>()
    private var isNetworkAvailable = false
    private var currentConnectedUserId: String? = null

    init {
        setupNetworkMonitoring()
        observeAuthenticationState()
    }

    private fun setupNetworkMonitoring() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
            return
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
                Log.d(tag, "Network available. Reconnecting Firestore listeners.")
                val userId = currentConnectedUserId ?: tokenManager.getUserIdSync()
                if (!userId.isNullOrBlank() && userId != "default_member") {
                    scope.launch {
                        _connectionStatus.value = FirestoreConnectionStatus.SYNCING
                        try {
                            firestoreSyncManager.syncAllLocalUserData(userId)
                            _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
                        } catch (e: Exception) {
                            Log.w(tag, "Post-reconnection sync encountered exception", e)
                            _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
                        }
                    }
                } else {
                    _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
                }
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
                Log.d(tag, "Network lost. Operating in offline persistent cache mode.")
                _connectionStatus.value = FirestoreConnectionStatus.OFFLINE
            }
        })
    }

    private fun observeAuthenticationState() {
        scope.launch {
            tokenManager.getUserId().collect { userId ->
                if (!userId.isNullOrBlank() && userId != "default_member") {
                    startRealtimeConnection(userId)
                } else {
                    stopRealtimeConnection()
                }
            }
        }
    }

    @Synchronized
    fun startRealtimeConnection(userId: String) {
        if (currentConnectedUserId == userId && activeRegistrations.isNotEmpty()) {
            return
        }

        stopRealtimeConnection()
        currentConnectedUserId = userId
        Log.i(tag, "Starting real-time connection for user: $userId")

        _connectionStatus.value = if (isNetworkAvailable) FirestoreConnectionStatus.SYNCING else FirestoreConnectionStatus.OFFLINE

        // 1. Initial bidirectional synchronization in background
        scope.launch {
            try {
                firestoreSyncManager.pullAllUserDataFromFirestore(userId)
                firestoreSyncManager.syncAllLocalUserData(userId)
                if (isNetworkAvailable) {
                    _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
                }
            } catch (e: Exception) {
                Log.w(tag, "Initial sync failed", e)
            }
        }

        // 2. Real-time User Profile Listener
        val profileReg = firestore.collection(FirestoreSyncManager.COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.w(tag, "User profile listener error", error)
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    updateStatusFromMetadata(doc.metadata.isFromCache, doc.metadata.hasPendingWrites())
                    scope.launch {
                        try {
                            val roleStr = doc.getString("role") ?: "MEMBER"
                            val role = try { Role.valueOf(roleStr) } catch (_: Exception) { Role.MEMBER }
                            val userEntity = UserEntity(
                                id = doc.getString("id") ?: doc.id,
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
                            userDao.upsertUser(userEntity)
                        } catch (e: Exception) {
                            Log.w(tag, "Failed to parse profile snapshot", e)
                        }
                    }
                }
            }
        activeRegistrations.add(profileReg)

        // 3. Real-time Workout Plan Listener (Active Plan)
        val planReg = firestore.collection(FirestoreSyncManager.COLLECTION_PLANS)
            .whereEqualTo("memberId", userId)
            .whereEqualTo("isActive", true)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Plan listener error", error)
                    return@addSnapshotListener
                }
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null && doc.exists()) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
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
                                memberId = doc.getString("memberId") ?: userId,
                                name = doc.getString("name") ?: "Custom Workout Plan",
                                version = doc.getLong("version")?.toInt() ?: 1,
                                isActive = doc.getBoolean("isActive") ?: true,
                                days = daysList
                            )
                            planDao.replaceCompletePlan(parsedPlan)
                        } catch (e: Exception) {
                            Log.w(tag, "Failed to parse active plan snapshot", e)
                        }
                    }
                }
            }
        activeRegistrations.add(planReg)

        // 3b. Real-time Trainer Plans Listener (when user acts as coach)
        val trainerPlanReg = firestore.collection(FirestoreSyncManager.COLLECTION_PLANS)
            .whereEqualTo("trainerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Trainer plans listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (doc in snapshot.documents) {
                            if (doc.exists()) {
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
                                        trainerId = doc.getString("trainerId") ?: userId,
                                        memberId = doc.getString("memberId") ?: "",
                                        name = doc.getString("name") ?: "Custom Workout Plan",
                                        version = doc.getLong("version")?.toInt() ?: 1,
                                        isActive = doc.getBoolean("isActive") ?: true,
                                        days = daysList
                                    )
                                    planDao.replaceCompletePlan(parsedPlan)
                                } catch (e: Exception) {
                                    Log.w(tag, "Failed to parse trainer plan snapshot", e)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(trainerPlanReg)

        // 4. Real-time Workout Sessions & Set Logs Listener
        val sessionsReg = firestore.collection(FirestoreSyncManager.COLLECTION_SESSIONS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Workout sessions listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val sessionId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                                    if (!isCompleted) {
                                        continue
                                    }
                                    val sessionEntity = WorkoutSessionEntity(
                                        id = sessionId,
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
                                            sessionId = map["sessionId"] as? String ?: sessionId,
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
                                DocumentChange.Type.REMOVED -> {
                                    // Two-way removal: deleted in cloud -> removed from local SQLite
                                    workoutSessionDao.deleteSetLogsForSession(sessionId)
                                    workoutSessionDao.deleteSession(sessionId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(sessionsReg)

        // 5. Real-time Hydration Logs Listener
        val hydrationReg = firestore.collection(FirestoreSyncManager.COLLECTION_HYDRATION_LOGS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Hydration listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val logId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val hydrationEntity = HydrationLogEntity(
                                        id = logId,
                                        memberId = doc.getString("memberId") ?: userId,
                                        amountMl = (doc.getLong("amountMl") ?: 0L).toInt(),
                                        dailyGoalMl = (doc.getLong("dailyGoalMl") ?: 3000L).toInt(),
                                        recordedAtEpochMs = doc.getLong("recordedAtEpochMs") ?: System.currentTimeMillis(),
                                        isSynced = true
                                    )
                                    weightDao.upsertHydrationLog(hydrationEntity)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    weightDao.deleteHydrationLog(logId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(hydrationReg)

        // 6. Real-time Weight Logs Listener
        val weightReg = firestore.collection(FirestoreSyncManager.COLLECTION_WEIGHT_LOGS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Weight listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val logId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val preWeight = (doc.getDouble("preWeightKg") ?: doc.getDouble("weightKg") ?: 0.0).toFloat()
                                    val postWeight = (doc.getDouble("postWeightKg") ?: 0.0).toFloat()
                                    val weightEntity = WeightLogEntity(
                                        id = logId,
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
                                DocumentChange.Type.REMOVED -> {
                                    weightDao.deleteWeightLog(logId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(weightReg)

        // 7. Real-time Body Measurements Listener
        val measurementReg = firestore.collection(FirestoreSyncManager.COLLECTION_MEASUREMENTS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Measurement listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val mId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val measurementEntity = BodyMeasurementEntity(
                                        id = mId,
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
                                DocumentChange.Type.REMOVED -> {
                                    weightDao.deleteMeasurement(mId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(measurementReg)

        // 8. Real-time Daily Steps Listener
        val stepsReg = firestore.collection(FirestoreSyncManager.COLLECTION_DAILY_STEPS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Daily steps listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val stepId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val stepsEntity = DailyStepsEntity(
                                        id = stepId,
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
                                DocumentChange.Type.REMOVED -> {
                                    dailyStepsDao.deleteDailySteps(stepId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(stepsReg)

        // 9. Real-time Outdoor Workouts Listener
        val outdoorReg = firestore.collection(FirestoreSyncManager.COLLECTION_OUTDOOR_WORKOUTS)
            .whereEqualTo("memberId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Outdoor workouts listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    updateStatusFromMetadata(snapshot.metadata.isFromCache, snapshot.metadata.hasPendingWrites())
                    scope.launch {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val workoutId = doc.getString("id") ?: doc.id

                            when (change.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val outdoorEntity = OutdoorWorkoutEntity(
                                        id = workoutId,
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
                                DocumentChange.Type.REMOVED -> {
                                    outdoorWorkoutDao.deleteOutdoorWorkout(workoutId)
                                }
                            }
                        }
                    }
                }
            }
        activeRegistrations.add(outdoorReg)
    }

    @Synchronized
    fun stopRealtimeConnection() {
        if (activeRegistrations.isNotEmpty()) {
            Log.d(tag, "Stopping ${activeRegistrations.size} real-time Firestore listeners")
            for (reg in activeRegistrations) {
                try { reg.remove() } catch (_: Exception) {}
            }
            activeRegistrations.clear()
        }
        currentConnectedUserId = null
        _connectionStatus.value = FirestoreConnectionStatus.OFFLINE
    }

    private fun updateStatusFromMetadata(isFromCache: Boolean, hasPendingWrites: Boolean) {
        if (!isNetworkAvailable) {
            _connectionStatus.value = FirestoreConnectionStatus.OFFLINE
        } else if (hasPendingWrites) {
            _connectionStatus.value = FirestoreConnectionStatus.SYNCING
        } else if (!isFromCache) {
            _connectionStatus.value = FirestoreConnectionStatus.CONNECTED
        }
    }
}

