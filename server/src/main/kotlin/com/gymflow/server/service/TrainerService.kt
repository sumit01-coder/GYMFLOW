package com.gymflow.server.service

import com.gymflow.server.database.DatabaseFactory.dbQuery
import com.gymflow.server.database.ExercisesTable
import com.gymflow.server.database.InvitationCodesTable
import com.gymflow.server.database.MemberProfilesTable
import com.gymflow.server.database.TrainerMembersTable
import com.gymflow.server.database.UsersTable
import com.gymflow.server.database.WorkoutDaysTable
import com.gymflow.server.database.WorkoutExercisesTable
import com.gymflow.server.database.WorkoutPlansTable
import com.gymflow.server.database.WorkoutSessionsTable
import com.gymflow.server.model.ConnectedMemberDto
import com.gymflow.server.model.CreatePlanRequest
import com.gymflow.server.model.InvitationCodeDto
import com.gymflow.server.model.WorkoutDayDto
import com.gymflow.server.model.WorkoutExerciseDto
import com.gymflow.server.model.WorkoutPlanDto
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.security.SecureRandom
import java.util.UUID

class TrainerService {

    private val random = SecureRandom()
    private val charPool = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    suspend fun getMembers(trainerId: String): List<ConnectedMemberDto> = dbQuery {
        val membersList = mutableListOf<ConnectedMemberDto>()

        val connections = TrainerMembersTable.selectAll()
            .where { TrainerMembersTable.trainerId eq trainerId }
            .toList()

        for (conn in connections) {
            val memberId = conn[TrainerMembersTable.memberId]
            val userRow = UsersTable.selectAll().where { UsersTable.id eq memberId }.singleOrNull() ?: continue
            val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq memberId }.singleOrNull()

            val latestSession = WorkoutSessionsTable.selectAll()
                .where { WorkoutSessionsTable.memberId eq memberId }
                .orderBy(WorkoutSessionsTable.startTime, SortOrder.DESC)
                .limit(1)
                .singleOrNull()

            val currentWorkout = if (latestSession != null && !latestSession[WorkoutSessionsTable.isCompleted]) {
                "${latestSession[WorkoutSessionsTable.workoutTitle]} (In Progress)"
            } else latestSession?.get(WorkoutSessionsTable.workoutTitle)

            membersList.add(
                ConnectedMemberDto(
                    requestId = conn[TrainerMembersTable.id],
                    memberId = memberId,
                    name = userRow[UsersTable.name],
                    email = userRow[UsersTable.email],
                    status = conn[TrainerMembersTable.status],
                    currentWorkout = currentWorkout ?: "Rest Day",
                    lastActive = latestSession?.get(WorkoutSessionsTable.startTime),
                    goal = profileRow?.get(MemberProfilesTable.goal)
                )
            )
        }

        membersList
    }

    suspend fun generateInvitationCode(trainerId: String): InvitationCodeDto = dbQuery {
        val trainerRow = UsersTable.selectAll().where { UsersTable.id eq trainerId }.singleOrNull()
            ?: throw NoSuchElementException("Trainer not found")

        val randomCode = "GYM-" + (1..5).map { charPool[random.nextInt(charPool.length)] }.joinToString("")
        val expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days

        InvitationCodesTable.insert {
            it[code] = randomCode
            it[InvitationCodesTable.trainerId] = trainerId
            it[InvitationCodesTable.expiresAt] = expiresAt
            it[isUsed] = false
        }

        InvitationCodeDto(
            code = randomCode,
            trainerId = trainerId,
            trainerName = trainerRow[UsersTable.name],
            expiresAtEpochMs = expiresAt,
            isUsed = false
        )
    }

    suspend fun getInvitations(trainerId: String): List<InvitationCodeDto> = dbQuery {
        val trainerRow = UsersTable.selectAll().where { UsersTable.id eq trainerId }.singleOrNull()
        val trainerName = trainerRow?.get(UsersTable.name) ?: "Personal Trainer"

        InvitationCodesTable.selectAll()
            .where { InvitationCodesTable.trainerId eq trainerId }
            .orderBy(InvitationCodesTable.expiresAt, SortOrder.DESC)
            .map {
                InvitationCodeDto(
                    code = it[InvitationCodesTable.code],
                    trainerId = it[InvitationCodesTable.trainerId],
                    trainerName = trainerName,
                    expiresAtEpochMs = it[InvitationCodesTable.expiresAt],
                    isUsed = it[InvitationCodesTable.isUsed]
                )
            }
    }

    suspend fun approveRequest(trainerId: String, requestId: String): Boolean = dbQuery {
        val connection = TrainerMembersTable.selectAll()
            .where { (TrainerMembersTable.id eq requestId) and (TrainerMembersTable.trainerId eq trainerId) }
            .singleOrNull() ?: return@dbQuery false

        val memberId = connection[TrainerMembersTable.memberId]

        TrainerMembersTable.update({ TrainerMembersTable.id eq requestId }) {
            it[status] = "ACTIVE"
        }

        MemberProfilesTable.update({ MemberProfilesTable.userId eq memberId }) {
            it[MemberProfilesTable.trainerId] = trainerId
        }

        true
    }

    suspend fun createWorkoutPlan(trainerId: String, request: CreatePlanRequest): WorkoutPlanDto = dbQuery {
        val planId = UUID.randomUUID().toString()

        // Deactivate previous active plans for this member
        WorkoutPlansTable.update({
            (WorkoutPlansTable.memberId eq request.memberId) and (WorkoutPlansTable.isActive eq true)
        }) {
            it[isActive] = false
        }

        WorkoutPlansTable.insert {
            it[id] = planId
            it[WorkoutPlansTable.trainerId] = trainerId
            it[memberId] = request.memberId
            it[name] = request.name
            it[version] = 1
            it[isActive] = true
        }

        val daysDtoList = mutableListOf<WorkoutDayDto>()

        for (day in request.days) {
            val generatedDayId = UUID.randomUUID().toString()
            WorkoutDaysTable.insert {
                it[id] = generatedDayId
                it[WorkoutDaysTable.planId] = planId
                it[dayOfWeek] = day.dayOfWeek
                it[title] = day.title
                it[isRestDay] = day.isRestDay
            }

            val exercisesDtoList = mutableListOf<WorkoutExerciseDto>()

            day.exercises.forEachIndexed { index, ex ->
                val exMappingId = UUID.randomUUID().toString()
                WorkoutExercisesTable.insert {
                    it[id] = exMappingId
                    it[WorkoutExercisesTable.dayId] = generatedDayId
                    it[exerciseId] = ex.exerciseId
                    it[position] = index
                    it[targetSets] = ex.targetSets
                    it[targetReps] = ex.targetReps
                    it[targetWeight] = ex.targetWeightKg
                    it[restSeconds] = ex.restDurationSeconds
                    it[notes] = ex.notes
                }

                exercisesDtoList.add(
                    ex.copy(id = exMappingId)
                )
            }

            daysDtoList.add(
                day.copy(id = generatedDayId, exercises = exercisesDtoList)
            )
        }

        WorkoutPlanDto(
            id = planId,
            trainerId = trainerId,
            memberId = request.memberId,
            name = request.name,
            version = 1,
            days = daysDtoList,
            isActive = true
        )
    }

    suspend fun getPlanById(planId: String): WorkoutPlanDto? = dbQuery {
        val planRow = WorkoutPlansTable.selectAll().where { WorkoutPlansTable.id eq planId }.singleOrNull()
            ?: return@dbQuery null

        val daysRows = WorkoutDaysTable.selectAll()
            .where { WorkoutDaysTable.planId eq planId }
            .toList()

        val daysList = daysRows.map { dayRow ->
            val dayId = dayRow[WorkoutDaysTable.id]
            val exerciseRows = WorkoutExercisesTable.selectAll()
                .where { WorkoutExercisesTable.dayId eq dayId }
                .orderBy(WorkoutExercisesTable.position, SortOrder.ASC)
                .toList()

            val exercises = exerciseRows.map { exRow ->
                val exerciseId = exRow[WorkoutExercisesTable.exerciseId]
                val baseEx = ExercisesTable.selectAll().where { ExercisesTable.id eq exerciseId }.singleOrNull()

                WorkoutExerciseDto(
                    id = exRow[WorkoutExercisesTable.id],
                    exerciseId = exerciseId,
                    exerciseName = baseEx?.get(ExercisesTable.name) ?: "Exercise",
                    category = baseEx?.get(ExercisesTable.category) ?: "General",
                    targetSets = exRow[WorkoutExercisesTable.targetSets],
                    targetReps = exRow[WorkoutExercisesTable.targetReps],
                    targetWeightKg = exRow[WorkoutExercisesTable.targetWeight],
                    restDurationSeconds = exRow[WorkoutExercisesTable.restSeconds],
                    notes = exRow[WorkoutExercisesTable.notes]
                )
            }

            WorkoutDayDto(
                id = dayId,
                dayOfWeek = dayRow[WorkoutDaysTable.dayOfWeek],
                title = dayRow[WorkoutDaysTable.title],
                isRestDay = dayRow[WorkoutDaysTable.isRestDay],
                exercises = exercises
            )
        }

        WorkoutPlanDto(
            id = planRow[WorkoutPlansTable.id],
            trainerId = planRow[WorkoutPlansTable.trainerId],
            memberId = planRow[WorkoutPlansTable.memberId],
            name = planRow[WorkoutPlansTable.name],
            version = planRow[WorkoutPlansTable.version],
            days = daysList,
            isActive = planRow[WorkoutPlansTable.isActive]
        )
    }

    suspend fun getPlansByTrainer(trainerId: String): List<WorkoutPlanDto> = dbQuery {
        val planRows = WorkoutPlansTable.selectAll()
            .where { WorkoutPlansTable.trainerId eq trainerId }
            .orderBy(WorkoutPlansTable.id, SortOrder.DESC)
            .toList()

        planRows.mapNotNull { getPlanById(it[WorkoutPlansTable.id]) }
    }
}
