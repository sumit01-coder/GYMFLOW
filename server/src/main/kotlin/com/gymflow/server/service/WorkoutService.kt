package com.gymflow.server.service

import com.gymflow.server.database.DatabaseFactory.dbQuery
import com.gymflow.server.database.ExercisesTable
import com.gymflow.server.database.MemberProfilesTable
import com.gymflow.server.database.SetLogsTable
import com.gymflow.server.database.UsersTable
import com.gymflow.server.database.WorkoutSessionsTable
import com.gymflow.server.model.CompleteWorkoutRequest
import com.gymflow.server.model.ExerciseDto
import com.gymflow.server.model.LiveWorkoutEvent
import com.gymflow.server.model.LogSetRequest
import com.gymflow.server.model.SetLogDto
import com.gymflow.server.model.StartWorkoutRequest
import com.gymflow.server.model.WorkoutSessionDto
import com.gymflow.server.sync.WebSocketManager
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class WorkoutService {

    suspend fun getAllExercises(): List<ExerciseDto> = dbQuery {
        ExercisesTable.selectAll().map {
            ExerciseDto(
                id = it[ExercisesTable.id],
                name = it[ExercisesTable.name],
                category = it[ExercisesTable.category],
                muscleGroup = it[ExercisesTable.muscleGroup],
                equipment = it[ExercisesTable.equipment],
                instructions = it[ExercisesTable.instructions],
                defaultRestSeconds = it[ExercisesTable.defaultRestSeconds]
            )
        }
    }

    suspend fun startSession(memberId: String, request: StartWorkoutRequest): WorkoutSessionDto {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val (sessionDto, memberName, trainerId) = dbQuery {
            WorkoutSessionsTable.insert {
                it[id] = sessionId
                it[WorkoutSessionsTable.memberId] = memberId
                it[planId] = request.planId
                it[workoutTitle] = request.workoutTitle
                it[startTime] = now
                it[endTime] = null
                it[durationSeconds] = 0L
                it[totalVolume] = 0f
                it[caloriesBurned] = 0
                it[isCompleted] = false
            }

            val userRow = UsersTable.selectAll().where { UsersTable.id eq memberId }.singleOrNull()
            val memberName = userRow?.get(UsersTable.name) ?: "Member"
            val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq memberId }.singleOrNull()
            val trainerId = profileRow?.get(MemberProfilesTable.trainerId)

            val dto = WorkoutSessionDto(
                id = sessionId,
                memberId = memberId,
                planId = request.planId,
                workoutTitle = request.workoutTitle,
                startTimeEpochMs = now,
                endTimeEpochMs = null,
                durationSeconds = 0L,
                totalVolumeKg = 0f,
                caloriesBurned = 0,
                isCompleted = false,
                setsCompleted = 0,
                totalSets = 0
            )

            Triple(dto, memberName, trainerId)
        }

        // Broadcast live start event
        WebSocketManager.broadcastLiveEvent(
            LiveWorkoutEvent(
                type = "WORKOUT_START",
                memberId = memberId,
                memberName = memberName,
                trainerId = trainerId,
                workoutTitle = request.workoutTitle,
                timestamp = now
            )
        )

        return sessionDto
    }

    suspend fun logSet(sessionId: String, request: LogSetRequest): SetLogDto {
        val setLogId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val (dto, memberId, memberName, trainerId, workoutTitle) = dbQuery {
            val sessionRow = WorkoutSessionsTable.selectAll()
                .where { WorkoutSessionsTable.id eq sessionId }
                .singleOrNull() ?: throw NoSuchElementException("Workout session not found")

            val memberId = sessionRow[WorkoutSessionsTable.memberId]
            val workoutTitle = sessionRow[WorkoutSessionsTable.workoutTitle]
            val currentVolume = sessionRow[WorkoutSessionsTable.totalVolume]
            val addedVolume = request.actualWeightKg * request.actualReps

            SetLogsTable.insert {
                it[id] = setLogId
                it[SetLogsTable.sessionId] = sessionId
                it[exerciseId] = request.exerciseId
                it[exerciseName] = request.exerciseName
                it[setNumber] = request.setNumber
                it[targetReps] = request.targetReps
                it[targetWeight] = request.targetWeightKg
                it[actualReps] = request.actualReps
                it[actualWeight] = request.actualWeightKg
                it[completedAt] = now
            }

            WorkoutSessionsTable.update({ WorkoutSessionsTable.id eq sessionId }) {
                it[totalVolume] = currentVolume + addedVolume
            }

            val userRow = UsersTable.selectAll().where { UsersTable.id eq memberId }.singleOrNull()
            val memberName = userRow?.get(UsersTable.name) ?: "Member"
            val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq memberId }.singleOrNull()
            val trainerId = profileRow?.get(MemberProfilesTable.trainerId)

            val setDto = SetLogDto(
                id = setLogId,
                sessionId = sessionId,
                exerciseId = request.exerciseId,
                exerciseName = request.exerciseName,
                setNumber = request.setNumber,
                targetReps = request.targetReps,
                targetWeightKg = request.targetWeightKg,
                actualReps = request.actualReps,
                actualWeightKg = request.actualWeightKg,
                completedAtEpochMs = now
            )

            SetBroadcastData(setDto, memberId, memberName, trainerId, workoutTitle)
        }

        // Broadcast live set completion event
        WebSocketManager.broadcastLiveEvent(
            LiveWorkoutEvent(
                type = "SET_COMPLETED",
                memberId = memberId,
                memberName = memberName,
                trainerId = trainerId,
                workoutTitle = workoutTitle,
                exerciseName = request.exerciseName,
                setNumber = request.setNumber,
                actualReps = request.actualReps,
                actualWeightKg = request.actualWeightKg,
                timestamp = now
            )
        )

        return dto
    }

    suspend fun completeSession(sessionId: String, request: CompleteWorkoutRequest): WorkoutSessionDto {
        val now = System.currentTimeMillis()

        val (dto, memberName, trainerId) = dbQuery {
            val sessionRow = WorkoutSessionsTable.selectAll()
                .where { WorkoutSessionsTable.id eq sessionId }
                .singleOrNull() ?: throw NoSuchElementException("Workout session not found")

            val memberId = sessionRow[WorkoutSessionsTable.memberId]

            WorkoutSessionsTable.update({ WorkoutSessionsTable.id eq sessionId }) {
                it[endTime] = now
                it[durationSeconds] = request.durationSeconds
                it[totalVolume] = request.totalVolumeKg
                it[caloriesBurned] = request.caloriesBurned
                it[isCompleted] = true
            }

            val userRow = UsersTable.selectAll().where { UsersTable.id eq memberId }.singleOrNull()
            val memberName = userRow?.get(UsersTable.name) ?: "Member"
            val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq memberId }.singleOrNull()
            val trainerId = profileRow?.get(MemberProfilesTable.trainerId)

            val sessionDto = WorkoutSessionDto(
                id = sessionId,
                memberId = memberId,
                planId = sessionRow[WorkoutSessionsTable.planId],
                workoutTitle = sessionRow[WorkoutSessionsTable.workoutTitle],
                startTimeEpochMs = sessionRow[WorkoutSessionsTable.startTime],
                endTimeEpochMs = now,
                durationSeconds = request.durationSeconds,
                totalVolumeKg = request.totalVolumeKg,
                caloriesBurned = request.caloriesBurned,
                isCompleted = true
            )

            Triple(sessionDto, memberName, trainerId)
        }

        // Broadcast workout finished event
        WebSocketManager.broadcastLiveEvent(
            LiveWorkoutEvent(
                type = "WORKOUT_FINISH",
                memberId = dto.memberId,
                memberName = memberName,
                trainerId = trainerId,
                workoutTitle = dto.workoutTitle,
                totalVolumeKg = request.totalVolumeKg,
                durationSeconds = request.durationSeconds,
                timestamp = now
            )
        )

        return dto
    }

    suspend fun getSessionById(sessionId: String): WorkoutSessionDto? = dbQuery {
        val sessionRow = WorkoutSessionsTable.selectAll()
            .where { WorkoutSessionsTable.id eq sessionId }
            .singleOrNull() ?: return@dbQuery null

        val setsCount = SetLogsTable.selectAll()
            .where { SetLogsTable.sessionId eq sessionId }
            .count().toInt()

        WorkoutSessionDto(
            id = sessionRow[WorkoutSessionsTable.id],
            memberId = sessionRow[WorkoutSessionsTable.memberId],
            planId = sessionRow[WorkoutSessionsTable.planId],
            workoutTitle = sessionRow[WorkoutSessionsTable.workoutTitle],
            startTimeEpochMs = sessionRow[WorkoutSessionsTable.startTime],
            endTimeEpochMs = sessionRow[WorkoutSessionsTable.endTime],
            durationSeconds = sessionRow[WorkoutSessionsTable.durationSeconds],
            totalVolumeKg = sessionRow[WorkoutSessionsTable.totalVolume],
            caloriesBurned = sessionRow[WorkoutSessionsTable.caloriesBurned],
            isCompleted = sessionRow[WorkoutSessionsTable.isCompleted],
            setsCompleted = setsCount,
            totalSets = setsCount
        )
    }

    suspend fun getSetsForSession(sessionId: String): List<SetLogDto> = dbQuery {
        SetLogsTable.selectAll()
            .where { SetLogsTable.sessionId eq sessionId }
            .orderBy(SetLogsTable.completedAt, SortOrder.ASC)
            .map {
                SetLogDto(
                    id = it[SetLogsTable.id],
                    sessionId = it[SetLogsTable.sessionId],
                    exerciseId = it[SetLogsTable.exerciseId],
                    exerciseName = it[SetLogsTable.exerciseName],
                    setNumber = it[SetLogsTable.setNumber],
                    targetReps = it[SetLogsTable.targetReps],
                    targetWeightKg = it[SetLogsTable.targetWeight],
                    actualReps = it[SetLogsTable.actualReps],
                    actualWeightKg = it[SetLogsTable.actualWeight],
                    completedAtEpochMs = it[SetLogsTable.completedAt]
                )
            }
    }

    private data class SetBroadcastData(
        val dto: SetLogDto,
        val memberId: String,
        val memberName: String,
        val trainerId: String?,
        val workoutTitle: String
    )
}
