package com.gymflow.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val token: String? = null,
    val heightCm: Float = 0f,
    val currentWeightKg: Float = 0f,
    val fitnessGoal: String = "General Fitness",
    val age: Int = 0,
    val connectedTrainerId: String? = null,
    val syncStatus: String = "Not Connected",
    val isEmailVerified: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        role = role,
        token = token,
        isEmailVerified = isEmailVerified
    )

    fun toMemberProfile(): MemberProfile = MemberProfile(
        userId = id,
        heightCm = heightCm,
        currentWeightKg = currentWeightKg,
        fitnessGoal = fitnessGoal,
        age = age,
        connectedTrainerId = connectedTrainerId,
        syncStatus = syncStatus
    )

    companion object {
        fun fromDomain(user: User, token: String? = user.token): UserEntity = UserEntity(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role,
            token = token,
            isEmailVerified = user.isEmailVerified
        )
    }
}
