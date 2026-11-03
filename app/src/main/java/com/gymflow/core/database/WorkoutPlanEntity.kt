package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.WorkoutPlan

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val trainerId: String,
    val memberId: String,
    val name: String,
    val version: Int = 1,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDomain(plan: WorkoutPlan): WorkoutPlanEntity = WorkoutPlanEntity(
            id = plan.id,
            trainerId = plan.trainerId,
            memberId = plan.memberId,
            name = plan.name,
            version = plan.version,
            isActive = plan.isActive
        )
    }
}
