package com.gymflow.feature.member

import com.gymflow.core.database.ExerciseDao
import com.gymflow.core.database.ExerciseEntity
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.WorkoutDayEntity
import com.gymflow.core.database.WorkoutDayWithExercises
import com.gymflow.core.database.WorkoutExerciseEntity
import com.gymflow.core.database.WorkoutPlanEntity
import com.gymflow.core.database.WorkoutPlanWithDays
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MemberScheduleCustomizationTest {

    private lateinit var fakePlanDao: FakePlanDao
    private lateinit var fakeExerciseDao: FakeExerciseDao

    private val memberId = "member_alex_rivera_042"

    @Before
    fun setUp() {
        fakePlanDao = FakePlanDao()
        fakeExerciseDao = FakeExerciseDao()
    }

    @Test
    fun testExerciseCatalogFilteringByCategoryAndEquipment() = runBlocking {
        val sampleExercises = listOf(
            Exercise(
                id = "ex_chest_bench",
                name = "Flat Barbell Bench Press",
                category = "Chest",
                muscleGroup = "Pectoralis Major",
                equipment = "Barbell",
                machineName = "Olympic Bench Press",
                defaultSets = 4,
                defaultReps = 8,
                defaultWeightKg = 60f
            ),
            Exercise(
                id = "ex_chest_pec_deck",
                name = "Seated Pec Deck Fly",
                category = "Chest",
                muscleGroup = "Inner Pectorals",
                equipment = "Machine",
                machineName = "Pec Deck Machine",
                defaultSets = 3,
                defaultReps = 12,
                defaultWeightKg = 45f
            ),
            Exercise(
                id = "ex_back_lat_pulldown",
                name = "Wide-Grip Lat Pulldown",
                category = "Back",
                muscleGroup = "Latissimus Dorsi",
                equipment = "Cable",
                machineName = "Cable Lat Pulldown Column",
                defaultSets = 4,
                defaultReps = 10,
                defaultWeightKg = 50f
            ),
            Exercise(
                id = "ex_leg_press",
                name = "45-Degree Incline Leg Press",
                category = "Legs",
                muscleGroup = "Quadriceps & Glutes",
                equipment = "Machine",
                machineName = "45° Sled Leg Press",
                defaultSets = 4,
                defaultReps = 12,
                defaultWeightKg = 120f
            ),
            Exercise(
                id = "ex_abs_cable_crunch",
                name = "Kneeling Cable Rope Crunch",
                category = "Abs",
                muscleGroup = "Rectus Abdominis",
                equipment = "Cable",
                machineName = "High Cable Column with Rope",
                defaultSets = 3,
                defaultReps = 15,
                defaultWeightKg = 25f
            )
        )

        fakeExerciseDao.upsertExercises(sampleExercises.map { ExerciseEntity.fromDomain(it) })

        // Filter by Category = "Chest"
        val chestExercises = fakeExerciseDao.getExercisesByCategory("Chest").first()
        assertEquals(2, chestExercises.size)
        assertTrue(chestExercises.all { it.category == "Chest" })

        // Filter by Category = "Chest" and Equipment = "Machine"
        val machineChest = fakeExerciseDao.getExercisesByCategoryAndEquipment("Chest", "Machine").first()
        assertEquals(1, machineChest.size)
        assertEquals("Seated Pec Deck Fly", machineChest[0].name)
        assertEquals("Pec Deck Machine", machineChest[0].machineName)

        // Search by query
        val searchResults = fakeExerciseDao.searchExercises("Cable").first()
        assertEquals(2, searchResults.size)
    }

    @Test
    fun testExerciseEntityRoomSerializationRoundTrip() {
        val original = Exercise(
            id = "ex_tri_pushdown",
            name = "Cable Rope Triceps Pushdown",
            category = "Triceps",
            muscleGroup = "Triceps Brachii",
            equipment = "Cable",
            machineName = "High Cable Pulley with Rope",
            defaultSets = 4,
            defaultReps = 12,
            defaultWeightKg = 22.5f,
            defaultRestSeconds = 45,
            instructions = listOf(
                "Pin elbows against ribcage.",
                "Extend arms downward until elbows lock.",
                "Spread rope handles outward at bottom."
            ),
            coachTip = "Spread rope handles outward at peak."
        )

        val entity = ExerciseEntity.fromDomain(original)
        assertEquals("ex_tri_pushdown", entity.id)
        assertEquals("Triceps", entity.category)
        assertEquals("High Cable Pulley with Rope", entity.machineName)

        val restored = entity.toDomain()
        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.instructions.size, restored.instructions.size)
        assertEquals(original.instructions[0], restored.instructions[0])
        assertEquals(original.coachTip, restored.coachTip)
    }

    @Test
    fun testMemberSelfScheduledWeeklyRoutineCreation() = runBlocking {
        val customPlan = WorkoutPlan(
            id = "plan_self_$memberId",
            trainerId = "self",
            memberId = memberId,
            name = "Alex's Push-Pull-Legs Routine",
            version = 1,
            isActive = true,
            days = listOf(
                WorkoutDay(
                    dayOfWeek = "Monday",
                    title = "Push (Chest & Triceps)",
                    exercises = listOf(
                        WorkoutExercise("ex_1", "Flat Barbell Bench Press", "Chest", 4, 8, 70f, 90, "Olympic Bench"),
                        WorkoutExercise("ex_2", "Cable Rope Triceps Pushdown", "Triceps", 3, 12, 25f, 45, "High Cable")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Tuesday",
                    title = "Pull (Back & Biceps)",
                    exercises = listOf(
                        WorkoutExercise("ex_3", "Wide-Grip Lat Pulldown", "Back", 4, 10, 55f, 60, "Cable Column"),
                        WorkoutExercise("ex_4", "Standing Barbell Bicep Curl", "Biceps", 3, 10, 25f, 45, "EZ Bar")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Wednesday",
                    title = "Legs & Abs",
                    exercises = listOf(
                        WorkoutExercise("ex_5", "Barbell Back Squat", "Legs", 4, 8, 85f, 90, "Power Cage"),
                        WorkoutExercise("ex_6", "Kneeling Cable Rope Crunch", "Abs", 3, 15, 30f, 45, "High Cable")
                    )
                ),
                WorkoutDay(
                    dayOfWeek = "Sunday",
                    title = "Rest & Recovery",
                    exercises = emptyList(),
                    isRestDay = true
                )
            )
        )

        // Save to Room via PlanDao
        fakePlanDao.replaceCompletePlan(customPlan)

        // Retrieve from PlanDao
        val retrieved = fakePlanDao.getActivePlanWithDays(memberId).first()
        assertNotNull(retrieved)
        val domainPlan = retrieved!!.toDomain()

        assertEquals("plan_self_$memberId", domainPlan.id)
        assertEquals("self", domainPlan.trainerId)
        assertEquals(4, domainPlan.days.size)

        val monday = domainPlan.days.find { it.dayOfWeek == "Monday" }
        assertNotNull(monday)
        assertEquals("Push (Chest & Triceps)", monday!!.title)
        assertEquals(2, monday.exercises.size)
        assertEquals("Flat Barbell Bench Press", monday.exercises[0].exerciseName)
        assertEquals(70f, monday.exercises[0].targetWeightKg)

        val sunday = domainPlan.days.find { it.dayOfWeek == "Sunday" }
        assertNotNull(sunday)
        assertTrue(sunday!!.isRestDay)
        assertTrue(sunday.exercises.isEmpty())
    }

    @Test
    fun testMemberAddAndRemoveExercisesFromDay() = runBlocking {
        val initialPlan = WorkoutPlan(
            id = "plan_self_$memberId",
            trainerId = "self",
            memberId = memberId,
            name = "Weekly Routine",
            days = listOf(
                WorkoutDay(
                    dayOfWeek = "Monday",
                    title = "Chest Focus",
                    exercises = listOf(
                        WorkoutExercise("ex_bench", "Flat Barbell Bench Press", "Chest", 4, 8, 60f)
                    )
                )
            )
        )
        fakePlanDao.replaceCompletePlan(initialPlan)

        // 1. Add new Machine exercise to Monday
        val newExercise = WorkoutExercise(
            exerciseId = "ex_pec_deck",
            exerciseName = "Seated Pec Deck Fly",
            category = "Chest",
            targetSets = 3,
            targetReps = 12,
            targetWeightKg = 45f,
            notes = "Pin-Loaded Pec Deck Machine"
        )
        val updatedDaysWithAdd = initialPlan.days.map { day ->
            if (day.dayOfWeek == "Monday") {
                day.copy(exercises = day.exercises + newExercise)
            } else day
        }
        fakePlanDao.replaceCompletePlan(initialPlan.copy(days = updatedDaysWithAdd))

        var activePlan = fakePlanDao.getActivePlanWithDays(memberId).first()!!.toDomain()
        val mondayUpdated = activePlan.days.find { it.dayOfWeek == "Monday" }!!
        assertEquals(2, mondayUpdated.exercises.size)
        assertEquals("Seated Pec Deck Fly", mondayUpdated.exercises[1].exerciseName)
        assertEquals("Pin-Loaded Pec Deck Machine", mondayUpdated.exercises[1].notes)

        // 2. Remove the first exercise
        val updatedDaysWithRemove = activePlan.days.map { day ->
            if (day.dayOfWeek == "Monday") {
                val list = day.exercises.toMutableList()
                list.removeAt(0)
                day.copy(exercises = list)
            } else day
        }
        fakePlanDao.replaceCompletePlan(activePlan.copy(days = updatedDaysWithRemove))

        activePlan = fakePlanDao.getActivePlanWithDays(memberId).first()!!.toDomain()
        val mondayFinal = activePlan.days.find { it.dayOfWeek == "Monday" }!!
        assertEquals(1, mondayFinal.exercises.size)
        assertEquals("Seated Pec Deck Fly", mondayFinal.exercises[0].exerciseName)
    }

    @Test
    fun testMemberToggleRestDayAndRename() = runBlocking {
        val plan = WorkoutPlan(
            id = "plan_self_$memberId",
            trainerId = "self",
            memberId = memberId,
            name = "Personal Routine",
            days = listOf(
                WorkoutDay(
                    dayOfWeek = "Wednesday",
                    title = "Leg Day",
                    exercises = listOf(
                        WorkoutExercise("ex_squat", "Barbell Back Squat", "Legs", 4, 8, 80f)
                    ),
                    isRestDay = false
                )
            )
        )
        fakePlanDao.replaceCompletePlan(plan)

        // Toggle Wednesday to Rest Day
        val restPlan = plan.copy(
            days = plan.days.map { day ->
                if (day.dayOfWeek == "Wednesday") day.copy(isRestDay = true) else day
            }
        )
        fakePlanDao.replaceCompletePlan(restPlan)

        var active = fakePlanDao.getActivePlanWithDays(memberId).first()!!.toDomain()
        assertTrue(active.days.first().isRestDay)

        // Rename title
        val renamedPlan = active.copy(
            days = active.days.map { day ->
                if (day.dayOfWeek == "Wednesday") day.copy(title = "Heavy Squat & Calves", isRestDay = false) else day
            }
        )
        fakePlanDao.replaceCompletePlan(renamedPlan)

        active = fakePlanDao.getActivePlanWithDays(memberId).first()!!.toDomain()
        assertEquals("Heavy Squat & Calves", active.days.first().title)
        assertFalse(active.days.first().isRestDay)
    }

    @Test
    fun testMemberHomeWorkoutBindingForCurrentDay() = runBlocking {
        val today = LocalDate.now()
        val currentDayFull = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)

        val fullPlan = WorkoutPlan(
            id = "plan_test",
            trainerId = "self",
            memberId = memberId,
            name = "Full Routine",
            days = listOf(
                WorkoutDay(
                    dayOfWeek = currentDayFull,
                    title = "Dynamic Today Workout",
                    exercises = listOf(
                        WorkoutExercise("ex_1", "Lat Pulldown Machine", "Back", 4, 10, 50f),
                        WorkoutExercise("ex_2", "Cable Row", "Back", 3, 12, 45f)
                    ),
                    isRestDay = false
                )
            )
        )
        fakePlanDao.replaceCompletePlan(fullPlan)

        val planWithDays = fakePlanDao.getActivePlanWithDays(memberId).first()
        assertNotNull(planWithDays)
        val domainPlan = planWithDays!!.toDomain()

        val matchingToday = domainPlan.days.find { it.dayOfWeek.equals(currentDayFull, ignoreCase = true) }
        assertNotNull(matchingToday)
        assertEquals("Dynamic Today Workout", matchingToday!!.title)
        assertEquals(2, matchingToday.exercises.size)
        assertEquals("Lat Pulldown Machine", matchingToday.exercises[0].exerciseName)
    }

    // ==================== Fake DAO Implementations ====================

    private class FakePlanDao : PlanDao {
        private val plansFlow = MutableStateFlow<Map<String, WorkoutPlanEntity>>(emptyMap())
        private val daysFlow = MutableStateFlow<Map<String, WorkoutDayEntity>>(emptyMap())
        private val exercisesFlow = MutableStateFlow<Map<String, WorkoutExerciseEntity>>(emptyMap())

        override suspend fun upsertPlan(plan: WorkoutPlanEntity) {
            plansFlow.value = plansFlow.value + (plan.id to plan)
        }

        override suspend fun upsertDays(days: List<WorkoutDayEntity>) {
            daysFlow.value = daysFlow.value + days.associateBy { it.id }
        }

        override suspend fun upsertExercises(exercises: List<WorkoutExerciseEntity>) {
            exercisesFlow.value = exercisesFlow.value + exercises.associateBy { it.id }
        }

        override fun getActivePlanWithDays(memberId: String): Flow<WorkoutPlanWithDays?> {
            return plansFlow.map { plans ->
                val plan = plans.values.find { it.memberId == memberId && it.isActive } ?: return@map null
                val days = daysFlow.value.values.filter { it.planId == plan.id }.sortedBy { it.orderIndex }
                val daysWithExercises = days.map { day ->
                    val exs = exercisesFlow.value.values.filter { it.dayId == day.id }.sortedBy { it.orderIndex }
                    WorkoutDayWithExercises(day, exs)
                }
                WorkoutPlanWithDays(plan, daysWithExercises)
            }
        }

        override fun getPlanWithDays(planId: String): Flow<WorkoutPlanWithDays?> {
            return plansFlow.map { plans ->
                val plan = plans[planId] ?: return@map null
                val days = daysFlow.value.values.filter { it.planId == plan.id }.sortedBy { it.orderIndex }
                val daysWithExercises = days.map { day ->
                    val exs = exercisesFlow.value.values.filter { it.dayId == day.id }.sortedBy { it.orderIndex }
                    WorkoutDayWithExercises(day, exs)
                }
                WorkoutPlanWithDays(plan, daysWithExercises)
            }
        }

        override suspend fun getPlanWithDaysSync(planId: String): WorkoutPlanWithDays? {
            return getPlanWithDays(planId).first()
        }

        override fun getPlansForMember(memberId: String): Flow<List<WorkoutPlanEntity>> {
            return plansFlow.map { it.values.filter { p -> p.memberId == memberId } }
        }

        override fun getPlansForTrainer(trainerId: String): Flow<List<WorkoutPlanEntity>> {
            return plansFlow.map { it.values.filter { p -> p.trainerId == trainerId } }
        }

        override suspend fun deletePlan(planId: String) {
            plansFlow.value = plansFlow.value - planId
        }

        override suspend fun deleteDaysForPlan(planId: String) {
            daysFlow.value = daysFlow.value.filterValues { it.planId != planId }
        }

        override suspend fun deleteExercisesForPlan(planId: String) {
            exercisesFlow.value = exercisesFlow.value.filterValues { it.planId != planId }
        }
    }

    private class FakeExerciseDao : ExerciseDao {
        private val exercisesFlow = MutableStateFlow<Map<String, ExerciseEntity>>(emptyMap())

        override suspend fun upsertExercises(exercises: List<ExerciseEntity>) {
            exercisesFlow.value = exercisesFlow.value + exercises.associateBy { it.id }
        }

        override suspend fun upsertExercise(exercise: ExerciseEntity) {
            exercisesFlow.value = exercisesFlow.value + (exercise.id to exercise)
        }

        override fun getAllExercises(): Flow<List<ExerciseEntity>> {
            return exercisesFlow.map { it.values.sortedBy { ex -> ex.name } }
        }

        override fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>> {
            return exercisesFlow.map { it.values.filter { ex -> ex.category.equals(category, ignoreCase = true) } }
        }

        override fun getExercisesByCategoryAndEquipment(
            category: String,
            equipment: String
        ): Flow<List<ExerciseEntity>> {
            return exercisesFlow.map {
                it.values.filter { ex ->
                    ex.category.equals(category, ignoreCase = true) && ex.equipment.equals(equipment, ignoreCase = true)
                }
            }
        }

        override fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>> {
            return exercisesFlow.map { it.values.filter { ex -> ex.equipment.equals(equipment, ignoreCase = true) } }
        }

        override fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
            return exercisesFlow.map {
                it.values.filter { ex ->
                    ex.name.contains(query, ignoreCase = true) ||
                            ex.category.contains(query, ignoreCase = true) ||
                            ex.equipment.contains(query, ignoreCase = true) ||
                            (ex.machineName?.contains(query, ignoreCase = true) == true) ||
                            ex.muscleGroup.contains(query, ignoreCase = true)
                }
            }
        }

        override suspend fun getExerciseById(id: String): ExerciseEntity? {
            return exercisesFlow.value[id]
        }

        override fun observeExerciseById(id: String): Flow<ExerciseEntity?> {
            return exercisesFlow.map { it[id] }
        }

        override suspend fun getExerciseCount(): Int {
            return exercisesFlow.value.size
        }

        override suspend fun clearAll() {
            exercisesFlow.value = emptyMap()
        }
    }
}

