package com.gymflow.domain.repository

import com.gymflow.core.common.Resource
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.WorkoutPlan
import com.gymflow.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getActivePlan(memberId: String): Flow<WorkoutPlan?>

    fun getPlanById(planId: String): Flow<WorkoutPlan?>

    fun savePlan(plan: WorkoutPlan): Flow<Resource<WorkoutPlan>>

    fun refreshPlanFromServer(memberId: String): Flow<Resource<WorkoutPlan>>

    fun startWorkoutSession(
        memberId: String,
        planId: String?,
        workoutTitle: String,
        totalSets: Int
    ): Flow<Resource<WorkoutSession>>

    fun recordSet(setLog: SetLog): Flow<Resource<Unit>>

    fun completeWorkoutSession(
        sessionId: String,
        durationSeconds: Long,
        totalVolumeKg: Float,
        caloriesBurned: Int
    ): Flow<Resource<WorkoutSession>>

    fun getActiveSession(memberId: String): Flow<WorkoutSession?>

    fun getRecentSessions(memberId: String): Flow<List<WorkoutSession>>

    fun getSessionWithSets(sessionId: String): Flow<Pair<WorkoutSession, List<SetLog>>?>

    fun refreshSessionsFromServer(memberId: String): Flow<Resource<List<WorkoutSession>>>
}
