package com.gymflow.server

import com.gymflow.server.config.ServerConfig
import com.gymflow.server.database.DatabaseFactory
import com.gymflow.server.plugins.configureCallLogging
import com.gymflow.server.plugins.configureCORS
import com.gymflow.server.plugins.configureRouting
import com.gymflow.server.plugins.configureSecurity
import com.gymflow.server.plugins.configureSerialization
import com.gymflow.server.plugins.configureSockets
import com.gymflow.server.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("com.gymflow.server.Application")
    logger.info("Initializing GYMFLOW Ktor Server on port ${ServerConfig.port} (Host: ${ServerConfig.host})...")

    try {
        DatabaseFactory.init()
    } catch (e: Exception) {
        logger.error("Database connection initialization warning: ${e.message}", e)
    }

    embeddedServer(
        factory = Netty,
        port = ServerConfig.port,
        host = ServerConfig.host,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureStatusPages()
    configureCORS()
    configureCallLogging()
    configureRouting()
}
