package com.gymflow.server.config

object ServerConfig {
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host: String = System.getenv("HOST") ?: "0.0.0.0"

    // Database Configuration
    val databaseUrl: String = System.getenv("DATABASE_URL")
        ?: "jdbc:postgresql://localhost:5432/gymflow"
    val databaseUser: String = System.getenv("DATABASE_USER") ?: "postgres"
    val databasePassword: String = System.getenv("DATABASE_PASSWORD") ?: "postgres"
    val databaseMaxConnections: Int = System.getenv("DATABASE_MAX_CONNECTIONS")?.toIntOrNull() ?: 10

    // JWT Configuration
    val jwtSecret: String = System.getenv("JWT_SECRET") ?: "gymflow-ultra-secure-and-robust-jwt-secret-key-2026"
    val jwtIssuer: String = System.getenv("JWT_ISSUER") ?: "com.gymflow.server"
    val jwtAudience: String = System.getenv("JWT_AUDIENCE") ?: "gymflow-app"
    val jwtRealm: String = System.getenv("JWT_REALM") ?: "GYMFLOW API"
    val jwtExpirationMs: Long = 7 * 24 * 60 * 60 * 1000L // 7 days
    val jwtRefreshExpirationMs: Long = 30 * 24 * 60 * 60 * 1000L // 30 days
}
