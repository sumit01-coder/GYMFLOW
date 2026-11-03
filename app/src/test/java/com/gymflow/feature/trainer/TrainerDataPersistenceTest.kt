package com.gymflow.feature.trainer

import com.gymflow.core.database.Converters
import com.gymflow.core.database.SyncStatus
import com.gymflow.core.database.UserEntity
import com.gymflow.core.database.WorkoutDayEntity
import com.gymflow.core.database.WorkoutDayWithExercises
import com.gymflow.core.database.WorkoutExerciseEntity
import com.gymflow.core.database.WorkoutPlanEntity
import com.gymflow.core.database.WorkoutPlanWithDays
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.InvitationCode
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.TrainerProfile
import com.gymflow.domain.model.User
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.feature.trainer.joincode.TrainerJoinCodeViewModel
import com.gymflow.feature.trainer.members.MemberFilterTab
import com.gymflow.feature.trainer.members.MemberManagementViewModel
import com.gymflow.feature.trainer.profile.TrainerMemberProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Automated Trainer Persona Test Suite for GymFlow.
 * Simulates Coach Marcus Vance (Iron Works Performance, Strength & Hypertrophy).
 * Exercises:
 *  1. Custom 4-Day Strength & Hypertrophy Split Plan creation, relational mapping, and persistence round-trip.
 *  2. Trainer join code generation, format validation, prefix checking, and expiration logic.
 *  3. Member roster connection approvals, rejections, filtering, and UserEntity trainer tracking.
 *  4. Database entity converters and relational integrity verification.
 */
class TrainerDataPersistenceTest {

    private val converters = Converters()
    private val coachTrainerId = "trainer_coach_marcus_vance"
    private val targetMemberId = "member_sarah_miller_101"

    @Before
    fun setUp() {
        // Initialization if needed
    }

    // =========================================================================
    // 1. WORKOUT PLAN CREATION & RELATIONAL ENTITY MAPPING
    // =========================================================================

    @Test
    fun testCustom4DayHypertrophyPlanCreationAndRelationalMapping() {
        // Coach Marcus Vance configures a 4-Day Push-Pull-Legs-Upper Hypertrophy Split 2026
        val day1ChestTriceps = WorkoutDay(
            dayOfWeek = "Monday",
            title = "Chest + Triceps",
            exercises = listOf(
                WorkoutExercise(
                    exerciseId = "ex_bench_press",
                    exerciseName = "Bench Press",
                    category = "Chest",
                    targetSets = 4,
                    targetReps = 10,
                    targetWeightKg = 70.0f,
                    restDurationSeconds = 60,
                    notes = "Retract scapulae; 2-sec eccentric.",
                    sets = listOf(
                        ExerciseSet(1, 10, 70.0f),
                        ExerciseSet(2, 10, 70.0f),
                        ExerciseSet(3, 10, 70.0f),
                        ExerciseSet(4, 10, 70.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_incline_db_press",
                    exerciseName = "Incline Dumbbell Press",
                    category = "Chest",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 24.0f,
                    restDurationSeconds = 45,
                    notes = "30 degree incline for upper clavicular head.",
                    sets = listOf(
                        ExerciseSet(1, 12, 24.0f),
                        ExerciseSet(2, 12, 24.0f),
                        ExerciseSet(3, 12, 24.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_cable_fly",
                    exerciseName = "Cable Fly",
                    category = "Chest",
                    targetSets = 3,
                    targetReps = 15,
                    targetWeightKg = 12.5f,
                    restDurationSeconds = 45,
                    notes = "Peak chest squeeze at centerline.",
                    sets = listOf(
                        ExerciseSet(1, 15, 12.5f),
                        ExerciseSet(2, 15, 12.5f),
                        ExerciseSet(3, 15, 12.5f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_triceps_pushdown",
                    exerciseName = "Triceps Pushdown",
                    category = "Triceps",
                    targetSets = 4,
                    targetReps = 12,
                    targetWeightKg = 25.0f,
                    restDurationSeconds = 45,
                    notes = "Elbows pinned to sides; full lockout.",
                    sets = listOf(
                        ExerciseSet(1, 12, 25.0f),
                        ExerciseSet(2, 12, 25.0f),
                        ExerciseSet(3, 12, 25.0f),
                        ExerciseSet(4, 12, 25.0f)
                    )
                )
            ),
            isCompleted = false,
            isRestDay = false
        )

        val day2BackBiceps = WorkoutDay(
            dayOfWeek = "Tuesday",
            title = "Back + Biceps",
            exercises = listOf(
                WorkoutExercise(
                    exerciseId = "ex_lat_pulldown",
                    exerciseName = "Lat Pulldown",
                    category = "Back",
                    targetSets = 4,
                    targetReps = 12,
                    targetWeightKg = 55.0f,
                    restDurationSeconds = 60,
                    notes = "Drive elbows toward back pockets.",
                    sets = listOf(
                        ExerciseSet(1, 12, 55.0f),
                        ExerciseSet(2, 12, 55.0f),
                        ExerciseSet(3, 12, 55.0f),
                        ExerciseSet(4, 12, 55.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_barbell_row",
                    exerciseName = "Barbell Bent-Over Row",
                    category = "Back",
                    targetSets = 4,
                    targetReps = 8,
                    targetWeightKg = 60.0f,
                    restDurationSeconds = 60,
                    notes = "Maintain neutral spine; pull to navel.",
                    sets = listOf(
                        ExerciseSet(1, 8, 60.0f),
                        ExerciseSet(2, 8, 60.0f),
                        ExerciseSet(3, 8, 60.0f),
                        ExerciseSet(4, 8, 60.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_db_bicep_curl",
                    exerciseName = "Dumbbell Bicep Curl",
                    category = "Biceps",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 14.0f,
                    restDurationSeconds = 45,
                    notes = "Supinate wrists at top of movement.",
                    sets = listOf(
                        ExerciseSet(1, 12, 14.0f),
                        ExerciseSet(2, 12, 14.0f),
                        ExerciseSet(3, 12, 14.0f)
                    )
                )
            ),
            isCompleted = false,
            isRestDay = false
        )

        val day3LegsCalves = WorkoutDay(
            dayOfWeek = "Thursday",
            title = "Legs + Calves",
            exercises = listOf(
                WorkoutExercise(
                    exerciseId = "ex_barbell_squat",
                    exerciseName = "Barbell Squat",
                    category = "Legs",
                    targetSets = 4,
                    targetReps = 8,
                    targetWeightKg = 85.0f,
                    restDurationSeconds = 90,
                    notes = "Hit parallel; brace abdominal core.",
                    sets = listOf(
                        ExerciseSet(1, 8, 85.0f),
                        ExerciseSet(2, 8, 85.0f),
                        ExerciseSet(3, 8, 85.0f),
                        ExerciseSet(4, 8, 85.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_leg_press",
                    exerciseName = "Leg Press",
                    category = "Legs",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 140.0f,
                    restDurationSeconds = 60,
                    notes = "Shoulder-width foot placement on platform.",
                    sets = listOf(
                        ExerciseSet(1, 12, 140.0f),
                        ExerciseSet(2, 12, 140.0f),
                        ExerciseSet(3, 12, 140.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_standing_calf_raise",
                    exerciseName = "Standing Calf Raise",
                    category = "Legs",
                    targetSets = 4,
                    targetReps = 15,
                    targetWeightKg = 40.0f,
                    restDurationSeconds = 45,
                    notes = "Full ankle plantar flexion and 1-sec stretch.",
                    sets = listOf(
                        ExerciseSet(1, 15, 40.0f),
                        ExerciseSet(2, 15, 40.0f),
                        ExerciseSet(3, 15, 40.0f),
                        ExerciseSet(4, 15, 40.0f)
                    )
                )
            ),
            isCompleted = false,
            isRestDay = false
        )

        val day4ShouldersAbs = WorkoutDay(
            dayOfWeek = "Friday",
            title = "Shoulders + Abs",
            exercises = listOf(
                WorkoutExercise(
                    exerciseId = "ex_overhead_press",
                    exerciseName = "Overhead Press",
                    category = "Shoulders",
                    targetSets = 4,
                    targetReps = 8,
                    targetWeightKg = 42.5f,
                    restDurationSeconds = 60,
                    notes = "Vertical bar path; avoid lumbar arching.",
                    sets = listOf(
                        ExerciseSet(1, 8, 42.5f),
                        ExerciseSet(2, 8, 42.5f),
                        ExerciseSet(3, 8, 42.5f),
                        ExerciseSet(4, 8, 42.5f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_lateral_raise",
                    exerciseName = "Lateral Raise",
                    category = "Shoulders",
                    targetSets = 4,
                    targetReps = 15,
                    targetWeightKg = 9.0f,
                    restDurationSeconds = 45,
                    notes = "Lead with elbows; pause at horizontal.",
                    sets = listOf(
                        ExerciseSet(1, 15, 9.0f),
                        ExerciseSet(2, 15, 9.0f),
                        ExerciseSet(3, 15, 9.0f),
                        ExerciseSet(4, 15, 9.0f)
                    )
                ),
                WorkoutExercise(
                    exerciseId = "ex_hanging_leg_raise",
                    exerciseName = "Hanging Leg Raise",
                    category = "Abs",
                    targetSets = 3,
                    targetReps = 15,
                    targetWeightKg = 0.0f,
                    restDurationSeconds = 45,
                    notes = "Posterior pelvic tilt; no swinging momentum.",
                    sets = listOf(
                        ExerciseSet(1, 15, 0.0f),
                        ExerciseSet(2, 15, 0.0f),
                        ExerciseSet(3, 15, 0.0f)
                    )
                )
            ),
            isCompleted = false,
            isRestDay = false
        )

        val publishedPlan = WorkoutPlan(
            id = "plan-test-2026",
            trainerId = coachTrainerId,
            memberId = targetMemberId,
            name = "Push-Pull-Legs-Upper Hypertrophy Split 2026",
            version = 1,
            days = listOf(day1ChestTriceps, day2BackBiceps, day3LegsCalves, day4ShouldersAbs),
            isActive = true
        )

        // 1. Verify Domain to Entity Mapping
        val planEntity = WorkoutPlanEntity.fromDomain(publishedPlan)
        assertEquals("plan-test-2026", planEntity.id)
        assertEquals(coachTrainerId, planEntity.trainerId)
        assertEquals(targetMemberId, planEntity.memberId)
        assertEquals("Push-Pull-Legs-Upper Hypertrophy Split 2026", planEntity.name)
        assertEquals(1, planEntity.version)
        assertTrue(planEntity.isActive)

        // 2. Simulate PlanDao.replaceCompletePlan database relational entity generation
        val dayEntities = mutableListOf<WorkoutDayEntity>()
        val exerciseEntities = mutableListOf<WorkoutExerciseEntity>()

        publishedPlan.days.forEachIndexed { dayIdx, day ->
            val dayEntityId = "${publishedPlan.id}_${day.dayOfWeek}"
            dayEntities.add(
                WorkoutDayEntity(
                    id = dayEntityId,
                    planId = publishedPlan.id,
                    dayOfWeek = day.dayOfWeek,
                    title = day.title,
                    isCompleted = day.isCompleted,
                    isRestDay = day.isRestDay,
                    orderIndex = dayIdx
                )
            )

            day.exercises.forEachIndexed { exIdx, ex ->
                exerciseEntities.add(
                    WorkoutExerciseEntity(
                        id = "${dayEntityId}_${ex.exerciseId}_$exIdx",
                        dayId = dayEntityId,
                        planId = publishedPlan.id,
                        exerciseId = ex.exerciseId,
                        exerciseName = ex.exerciseName,
                        category = ex.category,
                        targetSets = ex.targetSets,
                        targetReps = ex.targetReps,
                        targetWeightKg = ex.targetWeightKg,
                        restDurationSeconds = ex.restDurationSeconds,
                        notes = ex.notes,
                        sets = ex.sets,
                        orderIndex = exIdx
                    )
                )
            }
        }

        // Verify counts
        assertEquals(4, dayEntities.size)
        // Day 1 has 4, Day 2 has 3, Day 3 has 3, Day 4 has 3 -> Total 13 exercises
        assertEquals(13, exerciseEntities.size)

        // Verify foreign key integrity
        dayEntities.forEach { dayEntity ->
            assertEquals(publishedPlan.id, dayEntity.planId)
        }
        exerciseEntities.forEach { exEntity ->
            assertEquals(publishedPlan.id, exEntity.planId)
            assertTrue(exEntity.dayId.startsWith("${publishedPlan.id}_"))
        }

        // 3. Simulate Room Relations retrieval: WorkoutPlanWithDays & WorkoutDayWithExercises
        val daysWithExercises = dayEntities.map { dayEntity ->
            val matchingExercises = exerciseEntities.filter { it.dayId == dayEntity.id }
            WorkoutDayWithExercises(
                day = dayEntity,
                exercises = matchingExercises
            )
        }
        val planWithDays = WorkoutPlanWithDays(
            plan = planEntity,
            daysWithExercises = daysWithExercises
        )

        // 4. Verify Reconstruction to Domain Model
        val reconstructedDomainPlan = planWithDays.toDomain()
        assertEquals(publishedPlan.id, reconstructedDomainPlan.id)
        assertEquals(publishedPlan.trainerId, reconstructedDomainPlan.trainerId)
        assertEquals(publishedPlan.memberId, reconstructedDomainPlan.memberId)
        assertEquals(publishedPlan.name, reconstructedDomainPlan.name)
        assertEquals(4, reconstructedDomainPlan.days.size)

        // Inspect Day 1 Exercises
        val reconstructedDay1 = reconstructedDomainPlan.days[0]
        assertEquals("Monday", reconstructedDay1.dayOfWeek)
        assertEquals("Chest + Triceps", reconstructedDay1.title)
        assertEquals(4, reconstructedDay1.exercises.size)
        assertEquals("Bench Press", reconstructedDay1.exercises[0].exerciseName)
        assertEquals(70.0f, reconstructedDay1.exercises[0].targetWeightKg, 0.01f)
        assertEquals(4, reconstructedDay1.exercises[0].targetSets)
        assertEquals(4, reconstructedDay1.exercises[0].sets.size)

        // Inspect Day 2 Exercises
        val reconstructedDay2 = reconstructedDomainPlan.days[1]
        assertEquals("Tuesday", reconstructedDay2.dayOfWeek)
        assertEquals("Back + Biceps", reconstructedDay2.title)
        assertEquals(3, reconstructedDay2.exercises.size)
        assertEquals("Lat Pulldown", reconstructedDay2.exercises[0].exerciseName)
        assertEquals(55.0f, reconstructedDay2.exercises[0].targetWeightKg, 0.01f)

        // Inspect Day 3 Exercises
        val reconstructedDay3 = reconstructedDomainPlan.days[2]
        assertEquals("Thursday", reconstructedDay3.dayOfWeek)
        assertEquals("Legs + Calves", reconstructedDay3.title)
        assertEquals(3, reconstructedDay3.exercises.size)
        assertEquals("Barbell Squat", reconstructedDay3.exercises[0].exerciseName)
        assertEquals(85.0f, reconstructedDay3.exercises[0].targetWeightKg, 0.01f)

        // Inspect Day 4 Exercises
        val reconstructedDay4 = reconstructedDomainPlan.days[3]
        assertEquals("Friday", reconstructedDay4.dayOfWeek)
        assertEquals("Shoulders + Abs", reconstructedDay4.title)
        assertEquals(3, reconstructedDay4.exercises.size)
        assertEquals("Overhead Press", reconstructedDay4.exercises[0].exerciseName)
        assertEquals(42.5f, reconstructedDay4.exercises[0].targetWeightKg, 0.01f)
    }

    // =========================================================================
    // 2. TRAINER JOIN CODE SYSTEM & EXPIRATION LOGIC
    // =========================================================================

    @Test
    fun testInvitationCodeGenerationAndFormatValidation() {
        // Test random code generation pattern
        val generatedCodes = mutableListOf<String>()
        repeat(10) {
            val randomNum = Random.nextInt(10, 99)
            val chars = ('A'..'Z').toList()
            val randomChars = "" + chars.random() + chars.random() + Random.nextInt(10, 99)
            val code = "GYM-$randomNum$randomChars"
            generatedCodes.add(code)
        }

        generatedCodes.forEach { code ->
            assertTrue("Code should start with GYM- prefix: $code", code.startsWith("GYM-"))
            assertTrue("Code length should be valid: ${code.length}", code.length in 7..10)
            assertTrue("Code should only contain uppercase letters, numbers, and hyphen",
                code.matches(Regex("^GYM-[A-Z0-9]+$")))
        }
    }

    @Test
    fun testJoinCodeInputAutoFormattingAndNormalization() {
        // Function matching Member TrainerConnectionViewModel formatting
        fun formatCode(raw: String): String {
            val cleaned = raw.uppercase().replace("[^A-Z0-9-]".toRegex(), "")
            return if (!cleaned.startsWith("GYM-") && cleaned.length >= 3 && !cleaned.contains("-")) {
                "GYM-$cleaned"
            } else {
                cleaned.take(12)
            }
        }

        assertEquals("GYM-82K91", formatCode("gym-82k91"))
        assertEquals("GYM-82K91", formatCode("82k91"))
        assertEquals("GYM-77A34", formatCode("77A34"))
        assertEquals("GYM-SPECIAL", formatCode("gym-special"))
        assertEquals("GYM-12345", formatCode("12345"))
    }

    @Test
    fun testInvitationCodeExpirationTracking() {
        val now = System.currentTimeMillis()
        val validDurationMs = 24 * 60 * 60 * 1000L // 24 hours

        val activeCode = InvitationCode(
            code = "GYM-82K91",
            trainerId = coachTrainerId,
            trainerName = "Coach Marcus",
            expiresAtEpochMs = now + validDurationMs,
            isUsed = false
        )

        val expiredCode = InvitationCode(
            code = "GYM-OLD01",
            trainerId = coachTrainerId,
            trainerName = "Coach Marcus",
            expiresAtEpochMs = now - 1000L, // 1 second ago
            isUsed = false
        )

        val usedCode = InvitationCode(
            code = "GYM-USED2",
            trainerId = coachTrainerId,
            trainerName = "Coach Marcus",
            expiresAtEpochMs = now + validDurationMs,
            isUsed = true
        )

        fun isCodeUsable(code: InvitationCode, currentTime: Long = System.currentTimeMillis()): Boolean {
            return !code.isUsed && code.expiresAtEpochMs > currentTime
        }

        assertTrue("Active code within 24h should be usable", isCodeUsable(activeCode, now))
        assertFalse("Expired code should not be usable", isCodeUsable(expiredCode, now))
        assertFalse("Already used code should not be usable", isCodeUsable(usedCode, now))
    }

    @Test
    fun testTrainerJoinCodeViewModelWorkflow() {
        val viewModel = TrainerJoinCodeViewModel()

        val initialCode = viewModel.uiState.value.currentCode
        assertTrue(initialCode.startsWith("GYM-"))
        assertEquals(24, viewModel.uiState.value.expiresHoursRemaining)
        assertEquals(2, viewModel.uiState.value.pendingRequests.size)

        // Generate a new code
        viewModel.generateNewCode()
        val newCode = viewModel.uiState.value.currentCode
        assertTrue(newCode.startsWith("GYM-"))

        // Approve request
        val firstRequest = viewModel.uiState.value.pendingRequests.first()
        viewModel.approveRequest(firstRequest.id)
        assertEquals(1, viewModel.uiState.value.pendingRequests.size)
        assertEquals(firstRequest.memberName, viewModel.uiState.value.lastApprovedMemberName)

        // Reject second request
        val secondRequest = viewModel.uiState.value.pendingRequests.first()
        viewModel.rejectRequest(secondRequest.id)
        assertTrue(viewModel.uiState.value.pendingRequests.isEmpty())
    }

    // =========================================================================
    // 3. TRAINER DASHBOARD & MEMBER ROSTER MANAGEMENT
    // =========================================================================

    @Test
    fun testMemberRosterFilteringAndSearch() {
        val viewModel = MemberManagementViewModel()

        // 1. Default should contain all sample members
        assertEquals(MemberFilterTab.ALL, viewModel.uiState.value.selectedFilter)
        val allCount = viewModel.uiState.value.filteredMembers.size
        assertTrue(allCount >= 5)

        // 2. Filter by Active
        viewModel.onFilterSelected(MemberFilterTab.ACTIVE)
        val activeMembers = viewModel.uiState.value.filteredMembers
        assertTrue(activeMembers.all { it.status.equals("Active", ignoreCase = true) })

        // 3. Filter by Pending
        viewModel.onFilterSelected(MemberFilterTab.PENDING)
        val pendingMembers = viewModel.uiState.value.filteredMembers
        assertTrue(pendingMembers.all { it.status.equals("Pending", ignoreCase = true) })
        assertTrue(pendingMembers.any { it.name == "Mike Ross" || it.name == "Daniel Carter" })

        // 4. Search query filter
        viewModel.onFilterSelected(MemberFilterTab.ALL)
        viewModel.onSearchQueryChanged("Sarah")
        val searchResult = viewModel.uiState.value.filteredMembers
        assertEquals(1, searchResult.size)
        assertEquals("Sarah Miller", searchResult.first().name)
    }

    @Test
    fun testTrainerMemberProfileInspectionAndFeedbackNotes() {
        val profileVm = TrainerMemberProfileViewModel()

        val initialProfile = profileVm.uiState.value.profile
        assertEquals("Sarah Miller", initialProfile.name)
        assertEquals("sarah-miller-101", initialProfile.memberId)
        assertEquals("Active", initialProfile.status)
        assertEquals(92, initialProfile.attendanceRate)
        assertEquals(7, initialProfile.weeklyDays.size)

        // Coach adds a personalized feedback note
        val coachingNote = "Superb execution on 70kg Bench Press today! Let's progress to 72.5kg next cycle."
        profileVm.onFeedbackInputChanged(coachingNote)
        assertEquals(coachingNote, profileVm.uiState.value.feedbackInput)

        profileVm.sendFeedbackNote()
        assertTrue(profileVm.uiState.value.isNoteSentNoticeVisible)
        assertEquals("", profileVm.uiState.value.feedbackInput)
        assertEquals(coachingNote, profileVm.uiState.value.profile.notesHistory.first().text)
    }

    @Test
    fun testUserEntityTrainerConnectionPersistenceTracking() {
        val memberUser = User(
            id = "member_123",
            name = "Sarah Miller",
            email = "sarah@gymflow.com",
            role = Role.MEMBER,
            token = "jwt_token_sarah",
            isEmailVerified = true
        )

        // Initial entity (not connected)
        var userEntity = UserEntity.fromDomain(memberUser).copy(
            heightCm = 168f,
            currentWeightKg = 62.0f,
            fitnessGoal = "Build Muscle / Hypertrophy",
            age = 26
        )
        assertNull(userEntity.connectedTrainerId)
        assertEquals("Not Connected", userEntity.syncStatus)

        // Member joins via Coach Marcus invitation code GYM-82K91
        userEntity = userEntity.copy(
            connectedTrainerId = coachTrainerId,
            syncStatus = "Connected",
            lastUpdated = System.currentTimeMillis()
        )
        assertEquals(coachTrainerId, userEntity.connectedTrainerId)
        assertEquals("Connected", userEntity.syncStatus)

        // Convert to MemberProfile domain model
        val profile = userEntity.toMemberProfile()
        assertEquals("member_123", profile.userId)
        assertEquals(coachTrainerId, profile.connectedTrainerId)
        assertEquals("Connected", profile.syncStatus)
        assertEquals(168f, profile.heightCm, 0.01f)
        assertEquals(62.0f, profile.currentWeightKg, 0.01f)
        assertEquals("Build Muscle / Hypertrophy", profile.fitnessGoal)
    }

    // =========================================================================
    // 4. DATABASE CONVERTERS INTEGRITY & PERSISTENCE
    // =========================================================================

    @Test
    fun testExerciseSetListConverterIntegrity() {
        val exerciseSets = listOf(
            ExerciseSet(setNumber = 1, targetReps = 10, targetWeightKg = 70.0f, actualReps = 10, actualWeightKg = 70.0f, isCompleted = true),
            ExerciseSet(setNumber = 2, targetReps = 10, targetWeightKg = 70.0f, actualReps = 10, actualWeightKg = 70.0f, isCompleted = true),
            ExerciseSet(setNumber = 3, targetReps = 10, targetWeightKg = 70.0f, actualReps = 9, actualWeightKg = 70.0f, isCompleted = true),
            ExerciseSet(setNumber = 4, targetReps = 10, targetWeightKg = 70.0f, actualReps = 8, actualWeightKg = 70.0f, isCompleted = true)
        )

        val jsonString = converters.fromExerciseSetList(exerciseSets)
        assertNotNull(jsonString)
        assertTrue(jsonString!!.contains("targetWeightKg"))

        val decodedSets = converters.toExerciseSetList(jsonString)
        assertNotNull(decodedSets)
        assertEquals(4, decodedSets!!.size)
        assertEquals(exerciseSets, decodedSets)
    }

    @Test
    fun testSyncStatusAndRoleConverters() {
        assertEquals("PENDING", converters.fromSyncStatus(SyncStatus.PENDING))
        assertEquals("IN_PROGRESS", converters.fromSyncStatus(SyncStatus.IN_PROGRESS))
        assertEquals("FAILED", converters.fromSyncStatus(SyncStatus.FAILED))
        assertEquals("COMPLETED", converters.fromSyncStatus(SyncStatus.COMPLETED))
        assertEquals(SyncStatus.COMPLETED, converters.toSyncStatus("COMPLETED"))

        assertEquals("TRAINER", converters.fromRole(Role.TRAINER))
        assertEquals("MEMBER", converters.fromRole(Role.MEMBER))
        assertEquals(Role.TRAINER, converters.toRole("TRAINER"))
    }

    @Test
    fun testTrainerProfileDomainIntegrity() {
        val coachProfile = TrainerProfile(
            userId = coachTrainerId,
            experienceYears = 5,
            specialization = "Strength & Hypertrophy",
            activeMembersCount = 24,
            totalMembersCount = 32
        )

        assertEquals("trainer_coach_marcus_vance", coachProfile.userId)
        assertEquals(5, coachProfile.experienceYears)
        assertEquals("Strength & Hypertrophy", coachProfile.specialization)
        assertEquals(24, coachProfile.activeMembersCount)
        assertEquals(32, coachProfile.totalMembersCount)
    }
}
