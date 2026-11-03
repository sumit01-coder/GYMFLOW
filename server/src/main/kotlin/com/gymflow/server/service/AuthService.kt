package com.gymflow.server.service

import com.gymflow.server.database.DatabaseFactory.dbQuery
import com.gymflow.server.database.MemberProfilesTable
import com.gymflow.server.database.TrainerMembersTable
import com.gymflow.server.database.TrainerProfilesTable
import com.gymflow.server.database.UsersTable
import com.gymflow.server.model.AuthResponse
import com.gymflow.server.model.LoginRequest
import com.gymflow.server.model.MemberProfileDto
import com.gymflow.server.model.RegisterRequest
import com.gymflow.server.model.TrainerProfileDto
import com.gymflow.server.model.UserDto
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class AuthService {

    suspend fun register(request: RegisterRequest): AuthResponse = dbQuery {
        val existing = UsersTable.selectAll().where { UsersTable.email eq request.email.trim().lowercase() }.singleOrNull()
        if (existing != null) {
            throw IllegalArgumentException("A user with this email address already exists")
        }

        val userId = UUID.randomUUID().toString()
        val normalizedRole = request.role.trim().uppercase()
        if (normalizedRole != "MEMBER" && normalizedRole != "TRAINER") {
            throw IllegalArgumentException("Role must be either MEMBER or TRAINER")
        }

        val hashedPassword = PasswordHasher.hash(request.password)
        val now = System.currentTimeMillis()

        UsersTable.insert {
            it[id] = userId
            it[name] = request.name.trim()
            it[email] = request.email.trim().lowercase()
            it[passwordHash] = hashedPassword
            it[role] = normalizedRole
            it[createdAt] = now
        }

        if (normalizedRole == "MEMBER") {
            MemberProfilesTable.insert {
                it[MemberProfilesTable.userId] = userId
                it[height] = request.heightCm ?: 175.0f
                it[currentWeight] = request.currentWeightKg ?: 70.0f
                it[goal] = request.goal ?: "Build Muscle & Strength"
                it[age] = request.age ?: 25
                it[trainerId] = null
            }
        } else {
            TrainerProfilesTable.insert {
                it[TrainerProfilesTable.userId] = userId
                it[experience] = request.experienceYears ?: 3
                it[specialization] = request.specialization ?: "Strength & Conditioning"
            }
        }

        val token = JwtService.generateAccessToken(userId, request.email.trim().lowercase(), normalizedRole)
        val refreshToken = JwtService.generateRefreshToken(userId)

        val userDto = UserDto(
            id = userId,
            name = request.name.trim(),
            email = request.email.trim().lowercase(),
            role = normalizedRole,
            token = token
        )

        AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = userDto
        )
    }

    suspend fun login(request: LoginRequest): AuthResponse = dbQuery {
        val email = request.email.trim().lowercase()
        val row = UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()
            ?: throw IllegalArgumentException("Invalid email or password")

        val passwordHash = row[UsersTable.passwordHash]
        if (!PasswordHasher.verify(request.password, passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val userId = row[UsersTable.id]
        val role = row[UsersTable.role]
        val name = row[UsersTable.name]

        val token = JwtService.generateAccessToken(userId, email, role)
        val refreshToken = JwtService.generateRefreshToken(userId)

        val userDto = UserDto(
            id = userId,
            name = name,
            email = email,
            role = role,
            token = token
        )

        AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = userDto
        )
    }

    suspend fun getUserById(userId: String): UserDto? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()?.let { row ->
            UserDto(
                id = row[UsersTable.id],
                name = row[UsersTable.name],
                email = row[UsersTable.email],
                role = row[UsersTable.role]
            )
        }
    }

    suspend fun getMemberProfile(userId: String): MemberProfileDto? = dbQuery {
        val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull() ?: return@dbQuery null
        val profileRow = MemberProfilesTable.selectAll().where { MemberProfilesTable.userId eq userId }.singleOrNull()

        MemberProfileDto(
            userId = userId,
            name = userRow[UsersTable.name],
            email = userRow[UsersTable.email],
            heightCm = profileRow?.get(MemberProfilesTable.height) ?: 175f,
            currentWeightKg = profileRow?.get(MemberProfilesTable.currentWeight) ?: 70f,
            fitnessGoal = profileRow?.get(MemberProfilesTable.goal) ?: "Strength & Conditioning",
            age = profileRow?.get(MemberProfilesTable.age) ?: 25,
            connectedTrainerId = profileRow?.get(MemberProfilesTable.trainerId),
            syncStatus = if (profileRow?.get(MemberProfilesTable.trainerId) != null) "CONNECTED" else "DISCONNECTED"
        )
    }

    suspend fun getTrainerProfile(userId: String): TrainerProfileDto? = dbQuery {
        val userRow = UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull() ?: return@dbQuery null
        val profileRow = TrainerProfilesTable.selectAll().where { TrainerProfilesTable.userId eq userId }.singleOrNull()

        val activeMembers = TrainerMembersTable.selectAll().where {
            (TrainerMembersTable.trainerId eq userId) and (TrainerMembersTable.status eq "ACTIVE")
        }.count().toInt()

        val totalMembers = TrainerMembersTable.selectAll().where {
            TrainerMembersTable.trainerId eq userId
        }.count().toInt()

        TrainerProfileDto(
            userId = userId,
            name = userRow[UsersTable.name],
            email = userRow[UsersTable.email],
            experienceYears = profileRow?.get(TrainerProfilesTable.experience) ?: 3,
            specialization = profileRow?.get(TrainerProfilesTable.specialization) ?: "Strength & Hypertrophy",
            activeMembersCount = activeMembers,
            totalMembersCount = totalMembers
        )
    }
}
