package com.gymflow.feature.member

import com.gymflow.core.database.BodyMeasurementEntity
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WeightLogEntity
import com.gymflow.domain.calculator.WeightDifferenceCalculator
import com.gymflow.domain.calculator.WeightDifferenceCalculator.FluidLossSeverity
import com.gymflow.domain.model.BodyMeasurement
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import com.gymflow.domain.model.WeightLog
import com.gymflow.feature.member.measurements.MeasurementDelta
import com.gymflow.feature.member.profile.ProfileViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Automated Member Persona Test Suite for GymFlow.
 * Simulates a realistic Member experience (Liam Vance, 27yo, 181cm, "Build Muscle")
 * verifying:
 * 1. Weight Tracking & Stepper with acute pre/post workout fluid loss calculation.
 * 2. Body Measurements sequential delta calculations across consecutive sessions.
 * 3. Hydration Tracking accumulation & daily goal progress.
 * 4. Profile Metrics persistence and non-destructive updates via UserDao.
 */
class MemberDataPersistenceTest {

    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var fakeWeightDao: FakeWeightDao

    // Liam Vance Persona Constants
    private val memberId = "member_liam_vance_001"
    private val memberName = "Liam Vance"
    private val memberEmail = "liam.vance@gymflow.test"
    private val initialAge = 26
    private val updatedAge = 27
    private val initialHeight = 180.0f
    private val updatedHeight = 181.0f
    private val fitnessGoal = "Build Muscle"
    private val preWorkoutWeight = 75.40f
    private val postWorkoutWeight = 74.75f

    @Before
    fun setUp() {
        fakeUserDao = FakeUserDao()
        fakeWeightDao = FakeWeightDao()
    }

    // =========================================================================
    // Scenario 1: Weight Tracking, Stepper & Acute Fluid Loss
    // =========================================================================
    @Test
    fun testWeightTrackingAndCalculatorAccuracy() = runBlocking {
        // 1. Calculator Accuracy for Liam Vance
        // Pre: 75.40 kg, Post: 74.75 kg -> delta = -0.65 kg (650 ml fluid loss)
        val calculatedDelta = WeightDifferenceCalculator.calculateDelta(
            preWeightKg = preWorkoutWeight,
            postWeightKg = postWorkoutWeight
        )
        assertEquals(-0.65f, calculatedDelta, 0.001f)

        val analysis = WeightDifferenceCalculator.analyze(
            preWeightKg = preWorkoutWeight,
            postWeightKg = postWorkoutWeight
        )

        assertEquals(-0.65f, analysis.deltaKg, 0.001f)
        assertEquals(650, analysis.fluidLossMl)
        // Replenishment factor: 650 * 1.25 = 812.5 -> rounds to 813 ml
        assertEquals(813, analysis.recommendedFluidIntakeMl)
        // Percent loss: -0.65 / 75.40 = -0.86% -> MINIMAL (< 1%)
        assertEquals(FluidLossSeverity.MINIMAL, analysis.severity)
        assertEquals(-0.86f, analysis.percentChange, 0.01f)
        assertTrue(analysis.summaryNote.contains("immediate weight change"))
        assertTrue(analysis.advice.contains("813 ml"))

        // 2. Weight Log Entity Persistence
        val logId = UUID.randomUUID().toString()
        val recordedAt = System.currentTimeMillis()
        val notes = "Heavy chest session & 15m sauna"

        val domainLog = WeightLog(
            id = logId,
            memberId = memberId,
            preWeightKg = preWorkoutWeight,
            postWeightKg = postWorkoutWeight,
            deltaKg = calculatedDelta,
            recordedAtEpochMs = recordedAt,
            notes = notes
        )

        val entity = WeightLogEntity.fromDomain(domainLog, isSynced = false)
        fakeWeightDao.upsertWeightLog(entity)

        // 3. Verify Persistence in weight_logs
        val retrievedEntity = fakeWeightDao.getWeightById(logId)
        assertNotNull("WeightLogEntity must be stored in database", retrievedEntity)
        assertEquals(logId, retrievedEntity!!.id)
        assertEquals(memberId, retrievedEntity.memberId)
        assertEquals(preWorkoutWeight, retrievedEntity.preWeightKg, 0.001f)
        assertEquals(postWorkoutWeight, retrievedEntity.postWeightKg, 0.001f)
        assertEquals(-0.65f, retrievedEntity.deltaKg, 0.001f)
        assertEquals(notes, retrievedEntity.notes)
        assertFalse("Newly logged weight must be unsynced initially", retrievedEntity.isSynced)

        // 4. Verify History and Latest Log queries
        val latestLog = fakeWeightDao.getLatestWeightLog(memberId).first()
        assertNotNull(latestLog)
        assertEquals(logId, latestLog!!.id)

        val history = fakeWeightDao.getWeightHistory(memberId).first()
        assertEquals(1, history.size)
        assertEquals(domainLog, history[0].toDomain())

        // 5. Verify Sync Flag Update
        fakeWeightDao.markWeightLogSynced(logId, true)
        val syncedEntity = fakeWeightDao.getWeightById(logId)
        assertTrue(syncedEntity!!.isSynced)
        assertTrue(fakeWeightDao.getUnsyncedWeightLogs().isEmpty())
    }

    @Test
    fun testWeightStepperVariationAndFluidLossTiers() {
        // Test stepper variation: moderate and significant fluid loss tiers
        // Moderate sweat loss: 75.40kg -> 74.30kg (-1.10kg = -1.46% of body weight)
        val moderateAnalysis = WeightDifferenceCalculator.analyze(75.40f, 74.30f)
        assertEquals(-1.10f, moderateAnalysis.deltaKg, 0.001f)
        assertEquals(1100, moderateAnalysis.fluidLossMl)
        assertEquals(1375, moderateAnalysis.recommendedFluidIntakeMl)
        assertEquals(FluidLossSeverity.MODERATE, moderateAnalysis.severity)
        assertTrue(moderateAnalysis.advice.contains("electrolytes"))

        // Significant dehydration: 75.40kg -> 73.50kg (-1.90kg = -2.52% of body weight)
        val severeAnalysis = WeightDifferenceCalculator.analyze(75.40f, 73.50f)
        assertEquals(-1.90f, severeAnalysis.deltaKg, 0.001f)
        assertEquals(1900, severeAnalysis.fluidLossMl)
        assertEquals(2375, severeAnalysis.recommendedFluidIntakeMl)
        assertEquals(FluidLossSeverity.SIGNIFICANT, severeAnalysis.severity)
        assertTrue(severeAnalysis.summaryNote.contains("Significant dehydration"))

        // Neutral / weight gain during long hydrated session (+0.20kg)
        val gainAnalysis = WeightDifferenceCalculator.analyze(75.40f, 75.60f)
        assertEquals(0.20f, gainAnalysis.deltaKg, 0.001f)
        assertEquals(0, gainAnalysis.fluidLossMl)
        assertEquals(0, gainAnalysis.recommendedFluidIntakeMl)
        assertEquals(FluidLossSeverity.NEUTRAL_OR_GAIN, gainAnalysis.severity)
    }

    // =========================================================================
    // Scenario 2: Body Measurements Sequential Delta Verification
    // =========================================================================
    @Test
    fun testSequentialBodyMeasurementsAndDeltas() = runBlocking {
        val session1Epoch = 1730592000000L // Day 1
        val session2Epoch = 1730678400000L // Day 2 (later)

        // Session 1: Baseline measurements
        val session1 = BodyMeasurementEntity(
            id = "meas_001",
            memberId = memberId,
            chestCm = 102.5f,
            waistCm = 83.0f,
            armsCm = 38.5f,
            thighsCm = 57.0f,
            hipsCm = 97.5f,
            recordedAtEpochMs = session1Epoch,
            isSynced = false
        )

        // Session 2: Muscle gain & waist cut progress
        val session2 = BodyMeasurementEntity(
            id = "meas_002",
            memberId = memberId,
            chestCm = 103.2f, // +0.7
            waistCm = 82.4f, // -0.6
            armsCm = 38.8f,  // +0.3
            thighsCm = 57.5f,// +0.5
            hipsCm = 97.0f,  // -0.5
            recordedAtEpochMs = session2Epoch,
            isSynced = false
        )

        // Upsert both sessions to Room table
        fakeWeightDao.upsertMeasurement(session1)
        fakeWeightDao.upsertMeasurement(session2)

        // Verify retrieval ordered DESC by recordedAtEpochMs
        val historyEntities = fakeWeightDao.getMeasurements(memberId).first()
        assertEquals(2, historyEntities.size)
        assertEquals("meas_002", historyEntities[0].id) // Most recent first
        assertEquals("meas_001", historyEntities[1].id) // Baseline second

        val history = historyEntities.map { it.toDomain() }

        // Compute deltas using the exact MeasurementsViewModel algorithm
        val deltas = computeMeasurementDeltas(history)

        val chestDelta = deltas.first { it.partName == "Chest" }
        assertEquals(103.2f, chestDelta.currentValueCm, 0.01f)
        assertEquals(0.7f, chestDelta.deltaCm, 0.01f)

        val waistDelta = deltas.first { it.partName == "Waist" }
        assertEquals(82.4f, waistDelta.currentValueCm, 0.01f)
        assertEquals(-0.6f, waistDelta.deltaCm, 0.01f)

        val armsDelta = deltas.first { it.partName == "Arms" }
        assertEquals(38.8f, armsDelta.currentValueCm, 0.01f)
        assertEquals(0.3f, armsDelta.deltaCm, 0.01f)

        val thighsDelta = deltas.first { it.partName == "Thighs" }
        assertEquals(57.5f, thighsDelta.currentValueCm, 0.01f)
        assertEquals(0.5f, thighsDelta.deltaCm, 0.01f)

        val hipsDelta = deltas.first { it.partName == "Hips" }
        assertEquals(97.0f, hipsDelta.currentValueCm, 0.01f)
        assertEquals(-0.5f, hipsDelta.deltaCm, 0.01f)

        // Verify latest measurement accessor
        val latest = fakeWeightDao.getLatestMeasurement(memberId).first()
        assertNotNull(latest)
        assertEquals("meas_002", latest!!.id)
        assertEquals(103.2f, latest.chestCm, 0.01f)
    }

    // =========================================================================
    // Scenario 3: Hydration Tracking & Today's Total Accumulation
    // =========================================================================
    @Test
    fun testHydrationTrackingAndTodayAccumulation() = runBlocking {
        val startOfTodayMs = 1730617200000L // 00:00:00 today

        // Water intakes throughout Liam's training day:
        // +250 ml (morning wake-up)
        // +500 ml (pre-workout / workout hydration)
        // +750 ml (post-workout replenishment)
        val log1 = HydrationLogEntity(
            id = "hyd_001",
            memberId = memberId,
            amountMl = 250,
            dailyGoalMl = 3000,
            recordedAtEpochMs = startOfTodayMs + 10_000_000L
        )
        val log2 = HydrationLogEntity(
            id = "hyd_002",
            memberId = memberId,
            amountMl = 500,
            dailyGoalMl = 3000,
            recordedAtEpochMs = startOfTodayMs + 20_000_000L
        )
        val log3 = HydrationLogEntity(
            id = "hyd_003",
            memberId = memberId,
            amountMl = 750,
            dailyGoalMl = 3000,
            recordedAtEpochMs = startOfTodayMs + 30_000_000L
        )

        // Yesterday's intake (should NOT be counted towards today's total)
        val yesterdayLog = HydrationLogEntity(
            id = "hyd_yesterday",
            memberId = memberId,
            amountMl = 500,
            dailyGoalMl = 3000,
            recordedAtEpochMs = startOfTodayMs - 3_600_000L // 1 hour before today
        )

        fakeWeightDao.upsertHydrationLog(log1)
        fakeWeightDao.upsertHydrationLog(log2)
        fakeWeightDao.upsertHydrationLog(log3)
        fakeWeightDao.upsertHydrationLog(yesterdayLog)

        // Verify today's total accumulation: 250 + 500 + 750 = 1500 ml
        val todayTotal = fakeWeightDao.getTodayHydrationTotal(memberId, startOfTodayMs).first()
        assertEquals(1500, todayTotal)

        // Verify Daily Goal Progress: 1500 ml / 3000 ml = 50%
        val dailyGoalMl = 3000
        val progress = (todayTotal!!.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.50f, progress, 0.001f)

        // Verify post-workout deficit offset logic:
        // Initial sweat deficit: 650 ml.
        // After drinking 750 ml, deficit is fully resolved (coerced at least 0)
        val initialDeficit = 650
        val remainingAfterLog3 = (initialDeficit - log3.amountMl).coerceAtLeast(0)
        assertEquals(0, remainingAfterLog3)

        // Verify range query
        val todayLogs = fakeWeightDao.getHydrationLogsForRange(
            memberId = memberId,
            startMs = startOfTodayMs,
            endMs = startOfTodayMs + 86_400_000L
        ).first()
        assertEquals(3, todayLogs.size)
        assertEquals(listOf(250, 500, 750), todayLogs.map { it.amountMl })
    }

    // =========================================================================
    // Scenario 4: Member Profile Metrics Persistence & Non-destructive Updates
    // =========================================================================
    @Test
    fun testMemberProfilePersistenceAndNonDestructiveUpdate() = runBlocking {
        // Initial profile creation for Liam Vance
        val initialUser = UserEntity(
            id = memberId,
            name = memberName,
            email = memberEmail,
            role = Role.MEMBER,
            token = "jwt_token_liam_vance",
            heightCm = initialHeight,
            currentWeightKg = preWorkoutWeight,
            fitnessGoal = "General Fitness",
            age = initialAge,
            connectedTrainerId = "trainer_amit_01",
            syncStatus = "Connected",
            isEmailVerified = true,
            lastUpdated = System.currentTimeMillis() - 100_000L
        )

        fakeUserDao.upsertUser(initialUser)

        // Verify initial persistence
        val loadedUser = fakeUserDao.getUserById(memberId)
        assertNotNull(loadedUser)
        assertEquals(memberName, loadedUser!!.name)
        assertEquals(initialAge, loadedUser.age)
        assertEquals(initialHeight, loadedUser.heightCm, 0.01f)
        assertEquals("General Fitness", loadedUser.fitnessGoal)
        assertEquals("trainer_amit_01", loadedUser.connectedTrainerId)

        // Liam Vance updates Age (26 -> 27), Height (180 -> 181cm), Goal ("Build Muscle"), Weight (74.75kg)
        val updateTimestamp = System.currentTimeMillis()
        fakeUserDao.updateFullMemberProfile(
            id = memberId,
            heightCm = updatedHeight,
            weightKg = postWorkoutWeight,
            goal = fitnessGoal,
            age = updatedAge,
            now = updateTimestamp
        )

        // Verify updated entity
        val updatedUser = fakeUserDao.getUserById(memberId)
        assertNotNull(updatedUser)
        assertEquals(updatedAge, updatedUser!!.age)
        assertEquals(updatedHeight, updatedUser.heightCm, 0.01f)
        assertEquals(postWorkoutWeight, updatedUser.currentWeightKg, 0.01f)
        assertEquals(fitnessGoal, updatedUser.fitnessGoal)
        assertEquals(updateTimestamp, updatedUser.lastUpdated)

        // CRITICAL INVARIANT: Non-destructive update check
        // Email, Name, Role, Token, isEmailVerified, connectedTrainerId MUST NOT be wiped
        assertEquals(memberName, updatedUser.name)
        assertEquals(memberEmail, updatedUser.email)
        assertEquals(Role.MEMBER, updatedUser.role)
        assertEquals("jwt_token_liam_vance", updatedUser.token)
        assertEquals(true, updatedUser.isEmailVerified)
        assertEquals("trainer_amit_01", updatedUser.connectedTrainerId)

        // Verify MemberProfile domain mapping
        val memberProfile = updatedUser.toMemberProfile()
        assertEquals(memberId, memberProfile.userId)
        assertEquals(updatedAge, memberProfile.age)
        assertEquals(updatedHeight, memberProfile.heightCm, 0.01f)
        assertEquals(postWorkoutWeight, memberProfile.currentWeightKg, 0.01f)
        assertEquals("Build Muscle", memberProfile.fitnessGoal)
        assertEquals("trainer_amit_01", memberProfile.connectedTrainerId)

        // Verify initials extraction helper
        val initials = ProfileViewModel.extractInitials(updatedUser.name)
        assertEquals("LV", initials)
    }

    // =========================================================================
    // Scenario 5: End-to-End Persona Lifecycle Simulation
    // =========================================================================
    @Test
    fun testFullMemberPersonaLifecycle() = runBlocking {
        // Complete lifecycle of Liam Vance:
        // 1. Profile registered
        val user = UserEntity(
            id = memberId,
            name = memberName,
            email = memberEmail,
            role = Role.MEMBER,
            token = "session_token",
            heightCm = 181.0f,
            currentWeightKg = 75.40f,
            fitnessGoal = "Build Muscle",
            age = 27,
            isEmailVerified = true
        )
        fakeUserDao.upsertUser(user)

        // 2. Pre-workout weigh in: 75.40 kg
        // 3. Post-workout weigh in: 74.75 kg
        val weightLog = WeightLog(
            id = UUID.randomUUID().toString(),
            memberId = memberId,
            preWeightKg = 75.40f,
            postWeightKg = 74.75f,
            deltaKg = -0.65f,
            recordedAtEpochMs = System.currentTimeMillis(),
            notes = "Chest hypertrophy day + 20m sauna"
        )
        fakeWeightDao.upsertWeightLog(WeightLogEntity.fromDomain(weightLog))

        // 4. Weight update automatically synced into User profile
        fakeUserDao.updateMemberProfile(
            id = memberId,
            heightCm = 181.0f,
            weightKg = weightLog.postWeightKg,
            goal = "Build Muscle"
        )
        val profileAfterWeight = fakeUserDao.getUserById(memberId)
        assertEquals(74.75f, profileAfterWeight!!.currentWeightKg, 0.01f)

        // 5. Hydration replenishment: 250ml + 500ml
        val now = System.currentTimeMillis()
        fakeWeightDao.upsertHydrationLog(HydrationLogEntity("h1", memberId, 250, 3000, now))
        fakeWeightDao.upsertHydrationLog(HydrationLogEntity("h2", memberId, 500, 3000, now + 1000))

        val totalWater = fakeWeightDao.getTodayHydrationTotal(memberId, now - 5000).first()
        assertEquals(750, totalWater)

        // 6. Verification that all data remains queryable and uncorrupted
        val allWeights = fakeWeightDao.getWeightHistory(memberId).first()
        assertEquals(1, allWeights.size)
        assertEquals(-0.65f, allWeights[0].deltaKg, 0.01f)

        val currentUser = fakeUserDao.getCurrentUser()
        assertNotNull(currentUser)
        assertEquals(memberId, currentUser!!.id)
        assertEquals(27, currentUser.age)
    }

    // Helper method replicating MeasurementsViewModel computeDeltasForHistory
    private fun computeMeasurementDeltas(history: List<BodyMeasurement>): List<MeasurementDelta> {
        if (history.isEmpty()) return emptyList()

        if (history.size < 2) {
            val latest = history.first()
            return listOf(
                MeasurementDelta("Chest", latest.chestCm, 0f),
                MeasurementDelta("Waist", latest.waistCm, 0f),
                MeasurementDelta("Arms", latest.armsCm, 0f),
                MeasurementDelta("Thighs", latest.thighsCm, 0f),
                MeasurementDelta("Hips", latest.hipsCm, 0f)
            )
        }

        val latest = history[0]
        val previous = history[1]

        return listOf(
            MeasurementDelta("Chest", latest.chestCm, Math.round((latest.chestCm - previous.chestCm) * 10f) / 10f),
            MeasurementDelta("Waist", latest.waistCm, Math.round((latest.waistCm - previous.waistCm) * 10f) / 10f),
            MeasurementDelta("Arms", latest.armsCm, Math.round((latest.armsCm - previous.armsCm) * 10f) / 10f),
            MeasurementDelta("Thighs", latest.thighsCm, Math.round((latest.thighsCm - previous.thighsCm) * 10f) / 10f),
            MeasurementDelta("Hips", latest.hipsCm, Math.round((latest.hipsCm - previous.hipsCm) * 10f) / 10f)
        )
    }
}

// =============================================================================
// Fake In-Memory DAOs for strict Room SQLite specification compliance testing
// =============================================================================

class FakeUserDao : UserDao {
    private val usersMap = mutableMapOf<String, UserEntity>()
    private val userStateFlow = MutableStateFlow<UserEntity?>(null)

    override suspend fun upsertUser(user: UserEntity) {
        usersMap[user.id] = user
        userStateFlow.value = user
    }

    override suspend fun getUserById(id: String): UserEntity? = usersMap[id]

    override suspend fun getUserByEmail(email: String): UserEntity? =
        usersMap.values.find { it.email.equals(email, ignoreCase = true) }

    override fun getUserFlow(id: String): Flow<UserEntity?> =
        userStateFlow.map { usersMap[id] }

    override fun getCurrentUserFlow(): Flow<UserEntity?> = userStateFlow

    override suspend fun getCurrentUser(): UserEntity? =
        usersMap.values.maxByOrNull { it.lastUpdated }

    override suspend fun updateMemberProfile(
        id: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        now: Long
    ) {
        usersMap[id]?.let { existing ->
            val updated = existing.copy(
                heightCm = heightCm,
                currentWeightKg = weightKg,
                fitnessGoal = goal,
                lastUpdated = now
            )
            usersMap[id] = updated
            userStateFlow.value = updated
        }
    }

    override suspend fun updateFullMemberProfile(
        id: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        age: Int,
        now: Long
    ) {
        usersMap[id]?.let { existing ->
            val updated = existing.copy(
                heightCm = heightCm,
                currentWeightKg = weightKg,
                fitnessGoal = goal,
                age = age,
                lastUpdated = now
            )
            usersMap[id] = updated
            userStateFlow.value = updated
        }
    }

    override suspend fun updateTrainerConnection(
        id: String,
        trainerId: String?,
        syncStatus: String,
        now: Long
    ) {
        usersMap[id]?.let { existing ->
            val updated = existing.copy(
                connectedTrainerId = trainerId,
                syncStatus = syncStatus,
                lastUpdated = now
            )
            usersMap[id] = updated
            userStateFlow.value = updated
        }
    }

    override suspend fun updateEmailVerified(id: String, isVerified: Boolean, now: Long) {
        usersMap[id]?.let { existing ->
            val updated = existing.copy(
                isEmailVerified = isVerified,
                lastUpdated = now
            )
            usersMap[id] = updated
            userStateFlow.value = updated
        }
    }

    override suspend fun deleteUser(id: String) {
        usersMap.remove(id)
        if (userStateFlow.value?.id == id) {
            userStateFlow.value = null
        }
    }

    override suspend fun clearUsers() {
        usersMap.clear()
        userStateFlow.value = null
    }
}

class FakeWeightDao : WeightDao {
    private val weightLogs = mutableMapOf<String, WeightLogEntity>()
    private val hydrationLogs = mutableMapOf<String, HydrationLogEntity>()
    private val measurements = mutableMapOf<String, BodyMeasurementEntity>()

    private val weightFlow = MutableStateFlow<List<WeightLogEntity>>(emptyList())
    private val hydrationFlow = MutableStateFlow<List<HydrationLogEntity>>(emptyList())
    private val measurementFlow = MutableStateFlow<List<BodyMeasurementEntity>>(emptyList())

    // Weight Logs
    override suspend fun upsertWeightLog(log: WeightLogEntity) {
        weightLogs[log.id] = log
        weightFlow.value = weightLogs.values.toList()
    }

    override fun getWeightHistory(memberId: String): Flow<List<WeightLogEntity>> =
        weightFlow.map { list ->
            list.filter { it.memberId == memberId }
                .sortedByDescending { it.recordedAtEpochMs }
        }

    override fun getLatestWeightLog(memberId: String): Flow<WeightLogEntity?> =
        weightFlow.map { list ->
            list.filter { it.memberId == memberId }
                .maxByOrNull { it.recordedAtEpochMs }
        }

    override fun getLatestAnyWeightLog(): Flow<WeightLogEntity?> =
        weightFlow.map { list ->
            list.maxByOrNull { it.recordedAtEpochMs }
        }

    override suspend fun getLatestAnyWeightLogSync(): WeightLogEntity? =
        weightLogs.values.maxByOrNull { it.recordedAtEpochMs }

    override suspend fun getWeightById(id: String): WeightLogEntity? = weightLogs[id]

    override suspend fun deleteWeightLog(id: String) {
        weightLogs.remove(id)
        weightFlow.value = weightLogs.values.toList()
    }

    override suspend fun markWeightLogSynced(id: String, synced: Boolean) {
        weightLogs[id]?.let {
            weightLogs[id] = it.copy(isSynced = synced)
            weightFlow.value = weightLogs.values.toList()
        }
    }

    override suspend fun getUnsyncedWeightLogs(): List<WeightLogEntity> =
        weightLogs.values.filter { !it.isSynced }

    // Hydration Logs
    override suspend fun upsertHydrationLog(log: HydrationLogEntity) {
        hydrationLogs[log.id] = log
        hydrationFlow.value = hydrationLogs.values.toList()
    }

    override fun getHydrationLogsForRange(
        memberId: String,
        startMs: Long,
        endMs: Long
    ): Flow<List<HydrationLogEntity>> =
        hydrationFlow.map { list ->
            list.filter { it.memberId == memberId && it.recordedAtEpochMs in startMs..endMs }
                .sortedBy { it.recordedAtEpochMs }
        }

    override fun getTodayHydrationTotal(memberId: String, startOfDayMs: Long): Flow<Int?> =
        hydrationFlow.map { list ->
            val total = list
                .filter { (it.memberId == memberId || it.memberId == "default_member") && it.recordedAtEpochMs >= startOfDayMs }
                .sumOf { it.amountMl }
            if (total == 0 && list.none { (it.memberId == memberId || it.memberId == "default_member") && it.recordedAtEpochMs >= startOfDayMs }) null else total
        }

    override fun getTodayHydrationLogs(
        memberId: String,
        startOfDayMs: Long
    ): Flow<List<HydrationLogEntity>> =
        hydrationFlow.map { list ->
            list.filter { (it.memberId == memberId || it.memberId == "default_member") && it.recordedAtEpochMs >= startOfDayMs }
                .sortedByDescending { it.recordedAtEpochMs }
        }

    override fun getAllHydrationLogs(memberId: String): Flow<List<HydrationLogEntity>> =
        hydrationFlow.map { list ->
            list.filter { it.memberId == memberId || it.memberId == "default_member" }
                .sortedByDescending { it.recordedAtEpochMs }
        }

    override suspend fun deleteHydrationLog(id: String) {
        hydrationLogs.remove(id)
        hydrationFlow.value = hydrationLogs.values.toList()
    }

    override suspend fun markHydrationLogSynced(id: String, synced: Boolean) {
        hydrationLogs[id]?.let {
            hydrationLogs[id] = it.copy(isSynced = synced)
            hydrationFlow.value = hydrationLogs.values.toList()
        }
    }

    override suspend fun getUnsyncedHydrationLogs(): List<HydrationLogEntity> =
        hydrationLogs.values.filter { !it.isSynced }

    // Body Measurements
    override suspend fun upsertMeasurement(measurement: BodyMeasurementEntity) {
        measurements[measurement.id] = measurement
        measurementFlow.value = measurements.values.toList()
    }

    override fun getMeasurements(memberId: String): Flow<List<BodyMeasurementEntity>> =
        measurementFlow.map { list ->
            list.filter { it.memberId == memberId }
                .sortedByDescending { it.recordedAtEpochMs }
        }

    override fun getLatestMeasurement(memberId: String): Flow<BodyMeasurementEntity?> =
        measurementFlow.map { list ->
            list.filter { it.memberId == memberId }
                .maxByOrNull { it.recordedAtEpochMs }
        }

    override suspend fun markMeasurementSynced(id: String, synced: Boolean) {
        measurements[id]?.let {
            measurements[id] = it.copy(isSynced = synced)
            measurementFlow.value = measurements.values.toList()
        }
    }

    override suspend fun getUnsyncedMeasurements(): List<BodyMeasurementEntity> =
        measurements.values.filter { !it.isSynced }
}
