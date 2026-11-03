package com.gymflow.core.database

import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import com.gymflow.domain.model.WeightLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersAndMappingTest {

    private val converters = Converters()

    @Test
    fun testRoleConverter() {
        val memberStr = converters.fromRole(Role.MEMBER)
        assertEquals("MEMBER", memberStr)
        assertEquals(Role.MEMBER, converters.toRole("MEMBER"))

        val trainerStr = converters.fromRole(Role.TRAINER)
        assertEquals("TRAINER", trainerStr)
        assertEquals(Role.TRAINER, converters.toRole("TRAINER"))
    }

    @Test
    fun testSyncStatusConverter() {
        val pendingStr = converters.fromSyncStatus(SyncStatus.PENDING)
        assertEquals("PENDING", pendingStr)
        assertEquals(SyncStatus.PENDING, converters.toSyncStatus("PENDING"))
    }

    @Test
    fun testStringListConverter() {
        val original = listOf("Chest", "Back", "Legs")
        val json = converters.fromStringList(original)
        assertNotNull(json)
        val decoded = converters.toStringList(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testExerciseSetListConverter() {
        val original = listOf(
            ExerciseSet(setNumber = 1, targetReps = 12, targetWeightKg = 20f, actualReps = 12, actualWeightKg = 20f, isCompleted = true),
            ExerciseSet(setNumber = 2, targetReps = 10, targetWeightKg = 22.5f, actualReps = 10, actualWeightKg = 22.5f, isCompleted = true)
        )
        val json = converters.fromExerciseSetList(original)
        assertNotNull(json)
        val decoded = converters.toExerciseSetList(json)
        assertEquals(original, decoded)
    }

    @Test
    fun testUserEntityMapping() {
        val user = User(
            id = "user_123",
            name = "Alex Smith",
            email = "alex@gymflow.com",
            role = Role.MEMBER,
            token = "jwt_token"
        )
        val entity = UserEntity.fromDomain(user)
        assertEquals(user.id, entity.id)
        assertEquals(user.name, entity.name)
        assertEquals(user.role, entity.role)
        assertEquals(user.token, entity.token)

        val domain = entity.toDomain()
        assertEquals(user, domain)
    }

    @Test
    fun testWeightLogEntityMapping() {
        val weightLog = WeightLog(
            id = "w_123",
            memberId = "mem_456",
            preWeightKg = 70.0f,
            postWeightKg = 69.4f,
            deltaKg = -0.6f,
            recordedAtEpochMs = 1700000000000L,
            notes = "Morning workout session"
        )
        val entity = WeightLogEntity.fromDomain(weightLog, isSynced = true)
        assertTrue(entity.isSynced)
        assertEquals(weightLog.deltaKg, entity.deltaKg, 0.001f)

        val domain = entity.toDomain()
        assertEquals(weightLog, domain)
    }
}
