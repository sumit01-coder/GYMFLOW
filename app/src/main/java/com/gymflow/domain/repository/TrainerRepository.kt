package com.gymflow.domain.repository

import com.gymflow.core.common.Resource
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.InvitationCode
import com.gymflow.domain.model.TrainerProfile
import com.gymflow.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface TrainerRepository {
    fun generateInvitationCode(trainerId: String): Flow<Resource<InvitationCode>>

    fun connectWithInvitationCode(code: String): Flow<Resource<TrainerProfile>>

    fun getConnectedMembers(trainerId: String): Flow<Resource<List<ConnectedMember>>>

    fun getTrainerProfile(trainerId: String): Flow<Resource<TrainerProfile>>

    fun assignPlanToMember(plan: WorkoutPlan, memberId: String): Flow<Resource<WorkoutPlan>>
}
