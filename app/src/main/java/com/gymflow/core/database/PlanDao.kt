package com.gymflow.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.gymflow.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Upsert
    suspend fun upsertPlan(plan: WorkoutPlanEntity)

    @Upsert
    suspend fun upsertDays(days: List<WorkoutDayEntity>)

    @Upsert
    suspend fun upsertExercises(exercises: List<WorkoutExerciseEntity>)

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE (memberId = :memberId OR memberId = 'default_member') AND isActive = 1 ORDER BY CASE WHEN memberId = :memberId THEN 0 ELSE 1 END, updatedAt DESC LIMIT 1")
    fun getActivePlanWithDays(memberId: String): Flow<WorkoutPlanWithDays?>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE (memberId = :memberId OR memberId = 'default_member') AND isActive = 1 ORDER BY CASE WHEN memberId = :memberId THEN 0 ELSE 1 END, updatedAt DESC LIMIT 1")
    suspend fun getActivePlanWithDaysSync(memberId: String): WorkoutPlanWithDays?

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE id = :planId LIMIT 1")
    fun getPlanWithDays(planId: String): Flow<WorkoutPlanWithDays?>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE id = :planId LIMIT 1")
    suspend fun getPlanWithDaysSync(planId: String): WorkoutPlanWithDays?

    @Query("SELECT * FROM workout_plans WHERE memberId = :memberId ORDER BY updatedAt DESC")
    fun getPlansForMember(memberId: String): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE trainerId = :trainerId ORDER BY updatedAt DESC")
    fun getPlansForTrainer(trainerId: String): Flow<List<WorkoutPlanEntity>>

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deletePlan(planId: String)

    @Query("DELETE FROM workout_days WHERE planId = :planId")
    suspend fun deleteDaysForPlan(planId: String)

    @Query("DELETE FROM workout_exercises WHERE planId = :planId")
    suspend fun deleteExercisesForPlan(planId: String)

    @Transaction
    suspend fun replaceCompletePlan(plan: WorkoutPlan) {
        // Upsert plan header
        upsertPlan(WorkoutPlanEntity.fromDomain(plan))

        // Delete existing days & exercises for this plan to keep it completely synchronized
        deleteExercisesForPlan(plan.id)
        deleteDaysForPlan(plan.id)

        val dayEntities = mutableListOf<WorkoutDayEntity>()
        val exerciseEntities = mutableListOf<WorkoutExerciseEntity>()

        plan.days.forEachIndexed { dayIdx, day ->
            val dayEntityId = "${plan.id}_${day.dayOfWeek}"
            dayEntities.add(
                WorkoutDayEntity(
                    id = dayEntityId,
                    planId = plan.id,
                    dayOfWeek = day.dayOfWeek,
                    title = day.title,
                    isCompleted = day.isCompleted,
                    isRestDay = day.isRestDay,
                    orderIndex = dayIdx
                )
            )

            day.exercises.forEachIndexed { exIdx, exercise ->
                exerciseEntities.add(
                    WorkoutExerciseEntity(
                        id = "${dayEntityId}_${exercise.exerciseId}_$exIdx",
                        dayId = dayEntityId,
                        planId = plan.id,
                        exerciseId = exercise.exerciseId,
                        exerciseName = exercise.exerciseName,
                        category = exercise.category,
                        targetSets = exercise.targetSets,
                        targetReps = exercise.targetReps,
                        targetWeightKg = exercise.targetWeightKg,
                        restDurationSeconds = exercise.restDurationSeconds,
                        notes = exercise.notes,
                        sets = exercise.sets,
                        orderIndex = exIdx
                    )
                )
            }
        }

        if (dayEntities.isNotEmpty()) {
            upsertDays(dayEntities)
        }
        if (exerciseEntities.isNotEmpty()) {
            upsertExercises(exerciseEntities)
        }
    }
}
