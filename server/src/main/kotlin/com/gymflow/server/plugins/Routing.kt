package com.gymflow.server.plugins

import com.gymflow.server.routes.authRoutes
import com.gymflow.server.routes.memberRoutes
import com.gymflow.server.routes.syncRoutes
import com.gymflow.server.routes.trainerRoutes
import com.gymflow.server.routes.webSocketRoutes
import com.gymflow.server.routes.workoutRoutes
import com.gymflow.server.service.AuthService
import com.gymflow.server.service.MemberService
import com.gymflow.server.service.SyncService
import com.gymflow.server.service.TrainerService
import com.gymflow.server.service.WorkoutService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val authService = AuthService()
    val trainerService = TrainerService()
    val memberService = MemberService()
    val workoutService = WorkoutService()
    val syncService = SyncService()

    routing {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "service" to "GYMFLOW Backend API",
                    "status" to "ONLINE",
                    "tagline" to "TRAIN · TRACK · IMPROVE",
                    "version" to "1.0.0"
                )
            )
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "status" to "UP",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }

        authRoutes(authService)
        trainerRoutes(trainerService)
        memberRoutes(memberService)
        workoutRoutes(workoutService)
        syncRoutes(syncService)
        webSocketRoutes()
    }
}
