package com.gymflow.core.database

import androidx.room.Embedded
import androidx.room.Relation
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan

data class WorkoutDayWithExercises(
    @Embedded val day: WorkoutDayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "dayId"
    )
    val exercises: List<WorkoutExerciseEntity>
)

data class WorkoutPlanWithDays(
    @Embedded val plan: WorkoutPlanEntity,
    @Relation(
        entity = WorkoutDayEntity::class,
        parentColumn = "id",
        entityColumn = "planId"
    )
    val daysWithExercises: List<WorkoutDayWithExercises>
) {
    fun toDomain(): WorkoutPlan {
        return WorkoutPlan(
            id = plan.id,
            trainerId = plan.trainerId,
            memberId = plan.memberId,
            name = plan.name,
            version = plan.version,
            days = daysWithExercises.sortedBy { it.day.orderIndex }.map { dwe ->
                WorkoutDay(
                    dayOfWeek = dwe.day.dayOfWeek,
                    title = dwe.day.title,
                    exercises = dwe.exercises.sortedBy { it.orderIndex }.map { ex ->
                        WorkoutExercise(
                            exerciseId = ex.exerciseId,
                            exerciseName = ex.exerciseName,
                            category = ex.category,
                            targetSets = ex.targetSets,
                            targetReps = ex.targetReps,
                            targetWeightKg = ex.targetWeightKg,
                            restDurationSeconds = ex.restDurationSeconds,
                            notes = ex.notes,
                            sets = ex.sets
                        )
                    },
                    isCompleted = dwe.day.isCompleted,
                    isRestDay = dwe.day.isRestDay
                )
            },
            isActive = plan.isActive
        )
    }
}
