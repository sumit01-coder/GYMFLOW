package com.gymflow.server.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.gymflow.server.config.ServerConfig
import java.util.Date

object JwtService {
    private val algorithm = Algorithm.HMAC256(ServerConfig.jwtSecret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(ServerConfig.jwtIssuer)
        .withAudience(ServerConfig.jwtAudience)
        .build()

    fun generateAccessToken(userId: String, email: String, role: String): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + ServerConfig.jwtExpirationMs)

        return JWT.create()
            .withIssuer(ServerConfig.jwtIssuer)
            .withAudience(ServerConfig.jwtAudience)
            .withSubject(userId)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: String): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + ServerConfig.jwtRefreshExpirationMs)

        return JWT.create()
            .withIssuer(ServerConfig.jwtIssuer)
            .withAudience(ServerConfig.jwtAudience)
            .withSubject(userId)
            .withClaim("userId", userId)
            .withClaim("type", "refresh")
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
}
