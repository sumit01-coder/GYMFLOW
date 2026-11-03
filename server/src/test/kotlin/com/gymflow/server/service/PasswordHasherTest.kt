package com.gymflow.server.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun testHashAndVerifySuccess() {
        val password = "StrongPassword123!"
        val hash = PasswordHasher.hash(password)

        assertNotEquals(password, hash)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun testVerifyFailureWithWrongPassword() {
        val password = "StrongPassword123!"
        val hash = PasswordHasher.hash(password)

        assertFalse(PasswordHasher.verify("WrongPassword", hash))
    }
}
