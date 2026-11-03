package com.gymflow.server.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class JwtServiceTest {

    @Test
    fun testGenerateAndVerifyAccessToken() {
        val userId = "user-12345"
        val email = "alex@example.com"
        val role = "MEMBER"

        val token = JwtService.generateAccessToken(userId, email, role)
        assertNotNull(token)

        val decoded = JwtService.verifier.verify(token)
        assertEquals(userId, decoded.subject)
        assertEquals(userId, decoded.getClaim("userId").asString())
        assertEquals(email, decoded.getClaim("email").asString())
        assertEquals(role, decoded.getClaim("role").asString())
    }

    @Test
    fun testGenerateRefreshToken() {
        val userId = "trainer-999"
        val refreshToken = JwtService.generateRefreshToken(userId)
        assertNotNull(refreshToken)

        val decoded = JwtService.verifier.verify(refreshToken)
        assertEquals(userId, decoded.subject)
        assertEquals("refresh", decoded.getClaim("type").asString())
    }
}
