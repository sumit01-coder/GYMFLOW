package com.gymflow.server.service

import com.gymflow.server.database.BodyMeasurementsTable
import com.gymflow.server.database.DatabaseFactory.dbQuery
import com.gymflow.server.database.ExercisesTable
import com.gymflow.server.database.HydrationLogsTable
import com.gymflow.server.database.InvitationCodesTable
import com.gymflow.server.database.MemberProfilesTable
import com.gymflow.server.database.TrainerMembersTable
import com.gymflow.server.database.UsersTable
import com.gymflow.server.database.WeightLogsTable
import com.gymflow.server.database.WorkoutDaysTable
import com.gymflow.server.database.WorkoutExercisesTable
import com.gymflow.server.database.WorkoutPlansTable
import com.gymflow.server.database.WorkoutSessionsTable
import com.gymflow.server.model.BodyMeasurementDto
import com.gymflow.server.model.HydrationLogDto
import com.gymflow.server.model.MemberProfileDto
import com.gymflow.server.model.MemberProgressDto
import com.gymflow.server.model.RecordBodyMeasurementRequest
import com.gymflow.server.model.RecordHydrationRequest
import com.gymflow.server.model.RecordWeightRequest
import com.gymflow.server.model.SimpleResponse
import com.gymflow.server.model.WeightLogDto
import com.gymflow.server.model.WorkoutDayDto
import com.gymflow.server.model.WorkoutExerciseDto
import com.gymflow.server.model.WorkoutPlanDto
import com.gymflow.server.model.WorkoutSessionDto
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class MemberService {

    suspend fun getActiveWorkoutPlan(memberId: String): WorkoutPlanDto? = dbQuery {
        val planRow = WorkoutPlansTable.selectAll()
            .where { (WorkoutPlansTable.memberId eq memberId) and (WorkoutPlansTable.isActive eq true) }
            .orderBy(WorkoutPlansTable.version, SortOrder.DESC)
            .limit(1)
            .singleOrNull() ?: return@dbQuery null

        val planId = planRow[WorkoutPlansTable.id]
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

    suspend fun joinTrainer(memberId: String, code: String): SimpleResponse = dbQuery {
        val normalizedCode = code.trim().uppercase()
        val inv = InvitationCodesTable.selectAll()
            .where { InvitationCodesTable.code eq normalizedCode }
            .singleOrNull() ?: throw IllegalArgumentException("Invalid invitation code")

        if (inv[InvitationCodesTable.isUsed]) {
            throw IllegalArgumentException("Invitation code has already been redeemed")
        }

        if (System.currentTimeMillis() > inv[InvitationCodesTable.expiresAt]) {
            throw IllegalArgumentException("Invitation code has expired")
        }

        val trainerId = inv[InvitationCodesTable.trainerId]

        InvitationCodesTable.update({ InvitationCodesTable.code eq normalizedCode }) {
            it[isUsed] = true
        }

        val existingConn = TrainerMembersTable.selectAll()
            .where { (TrainerMembersTable.trainerId eq trainerId) and (TrainerMembersTable.memberId eq memberId) }
            .singleOrNull()

        if (existingConn != null) {
            TrainerMembersTable.update({ TrainerMembersTable.id eq existingConn[TrainerMembersTable.id] }) {
                it[status] = "PENDING"
            }
        } else {
            TrainerMembersTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[TrainerMembersTable.trainerId] = trainerId
                it[TrainerMembersTable.memberId] = memberId
                it[status] = "PENDING"
            }
        }

        MemberProfilesTable.update({ MemberProfilesTable.userId eq memberId }) {
            it[MemberProfilesTable.trainerId] = trainerId
        }

        SimpleResponse(
            success = true,
            message = "Successfully linked to trainer with pending confirmation"
        )
    }

    suspend fun recordWeight(memberId: String, request: RecordWeightRequest): WeightLogDto = dbQuery {
        val logId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val delta = request.postWeightKg - request.preWeightKg

        WeightLogsTable.insert {
            it[id] = logId
            it[WeightLogsTable.memberId] = memberId
            it[preWeight] = request.preWeightKg
            it[postWeight] = request.postWeightKg
            it[WeightLogsTable.delta] = delta
            it[recordedAt] = now
            it[notes] = request.notes
        }

        MemberProfilesTable.update({ MemberProfilesTable.userId eq memberId }) {
            it[currentWeight] = request.postWeightKg
        }

        WeightLogDto(
            id = logId,
            memberId = memberId,
            preWeightKg = request.preWeightKg,
            postWeightKg = request.postWeightKg,
            deltaKg = delta,
            recordedAtEpochMs = now,
            notes = request.notes
        )
    }

    suspend fun getWeightHistory(memberId: String): List<WeightLogDto> = dbQuery {
        WeightLogsTable.selectAll()
            .where { WeightLogsTable.memberId eq memberId }
            .orderBy(WeightLogsTable.recordedAt, SortOrder.DESC)
            .map {
                WeightLogDto(
                    id = it[WeightLogsTable.id],
                    memberId = it[WeightLogsTable.memberId],
                    preWeightKg = it[WeightLogsTable.preWeight],
                    postWeightKg = it[WeightLogsTable.postWeight],
                    deltaKg = it[WeightLogsTable.delta],
                    recordedAtEpochMs = it[WeightLogsTable.recordedAt],
                    notes = it[WeightLogsTable.notes]
                )
            }
    }

    suspend fun recordHydration(memberId: String, request: RecordHydrationRequest): HydrationLogDto = dbQuery {
        val logId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        HydrationLogsTable.insert {
            it[id] = logId
            it[HydrationLogsTable.memberId] = memberId
            it[amountMl] = request.amountMl
            it[recordedAt] = now
        }

        HydrationLogDto(
            id = logId,
            memberId = memberId,
            amountMl = request.amountMl,
            recordedAtEpochMs = now
        )
    }

    suspend fun recordMeasurements(memberId: String, request: RecordBodyMeasurementRequest): BodyMeasurementDto = dbQuery {
        val logId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        BodyMeasurementsTable.insert {
            it[id] = logId
            it[BodyMeasurementsTable.memberId] = memberId
            it[chest] = request.chestCm
            it[waist] = request.waistCm
            it[arms] = request.armsCm
            it[thighs] = request.thighsCm
            it[hips] = request.hipsCm
            it[recordedAt] = now
        }

        BodyMeasurementDto(
            id = logId,
            memberId = memberId,
            chestCm = request.chestCm,
            waistCm = request.waistCm,
            armsCm = request.armsCm,
            thighsCm = request.thighsCm,
            hipsCm = request.hipsCm,
            recordedAtEpochMs = now
        )
    }

    suspend fun getProgress(memberId: String): MemberProgressDto = dbQuery {
        val userRow = UsersTable.selectAll().where { UsersTable.id eq memberId }.singleOrNull()
            ?: throw NoSuchElementException("Member not found")
        val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq memberId }.singleOrNull()

        val recentWeights = WeightLogsTable.selectAll()
            .where { WeightLogsTable.memberId eq memberId }
            .orderBy(WeightLogsTable.recordedAt, SortOrder.DESC)
            .limit(10)
            .map {
                WeightLogDto(
                    id = it[WeightLogsTable.id],
                    memberId = it[WeightLogsTable.memberId],
                    preWeightKg = it[WeightLogsTable.preWeight],
                    postWeightKg = it[WeightLogsTable.postWeight],
                    deltaKg = it[WeightLogsTable.delta],
                    recordedAtEpochMs = it[WeightLogsTable.recordedAt],
                    notes = it[WeightLogsTable.notes]
                )
            }

        val latestMeasurement = BodyMeasurementsTable.selectAll()
            .where { BodyMeasurementsTable.memberId eq memberId }
            .orderBy(BodyMeasurementsTable.recordedAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()?.let {
                BodyMeasurementDto(
                    id = it[BodyMeasurementsTable.id],
                    memberId = it[BodyMeasurementsTable.memberId],
                    chestCm = it[BodyMeasurementsTable.chest],
                    waistCm = it[BodyMeasurementsTable.waist],
                    armsCm = it[BodyMeasurementsTable.arms],
                    thighsCm = it[BodyMeasurementsTable.thighs],
                    hipsCm = it[BodyMeasurementsTable.hips],
                    recordedAtEpochMs = it[BodyMeasurementsTable.recordedAt]
                )
            }

        val completedSessions = WorkoutSessionsTable.selectAll()
            .where { (WorkoutSessionsTable.memberId eq memberId) and (WorkoutSessionsTable.isCompleted eq true) }
            .orderBy(WorkoutSessionsTable.startTime, SortOrder.DESC)
            .toList()

        val totalWorkouts = completedSessions.size
        val totalVolume = completedSessions.map { it[WorkoutSessionsTable.totalVolume] }.sum()

        val recentSessions = completedSessions.take(5).map {
            WorkoutSessionDto(
                id = it[WorkoutSessionsTable.id],
                memberId = it[WorkoutSessionsTable.memberId],
                planId = it[WorkoutSessionsTable.planId],
                workoutTitle = it[WorkoutSessionsTable.workoutTitle],
                startTimeEpochMs = it[WorkoutSessionsTable.startTime],
                endTimeEpochMs = it[WorkoutSessionsTable.endTime],
                durationSeconds = it[WorkoutSessionsTable.durationSeconds],
                totalVolumeKg = it[WorkoutSessionsTable.totalVolume],
                caloriesBurned = it[WorkoutSessionsTable.caloriesBurned],
                isCompleted = it[WorkoutSessionsTable.isCompleted]
            )
        }

        MemberProgressDto(
            memberProfile = MemberProfileDto(
                userId = memberId,
                name = userRow[UsersTable.name],
                email = userRow[UsersTable.email],
                heightCm = profileRow?.get(MemberProfilesTable.height) ?: 175f,
                currentWeightKg = profileRow?.get(MemberProfilesTable.currentWeight) ?: 70f,
                fitnessGoal = profileRow?.get(MemberProfilesTable.goal) ?: "Strength & Conditioning",
                age = profileRow?.get(MemberProfilesTable.age) ?: 25,
                connectedTrainerId = profileRow?.get(MemberProfilesTable.trainerId),
                syncStatus = if (profileRow?.get(MemberProfilesTable.trainerId) != null) "CONNECTED" else "DISCONNECTED"
            ),
            recentWeights = recentWeights,
            latestMeasurements = latestMeasurement,
            totalWorkoutsCompleted = totalWorkouts,
            totalVolumeLiftedKg = totalVolume,
            recentSessions = recentSessions
        )
    }
}
