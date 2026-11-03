package com.gymflow.server.routes

import com.gymflow.server.model.LoginRequest
import com.gymflow.server.model.RegisterRequest
import com.gymflow.server.plugins.ResourceNotFoundException
import com.gymflow.server.plugins.getUserId
import com.gymflow.server.plugins.getUserRole
import com.gymflow.server.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            if (request.name.isBlank()) {
                throw IllegalArgumentException("Name cannot be empty")
            }
            if (request.email.isBlank() || !request.email.contains("@")) {
                throw IllegalArgumentException("A valid email address is required")
            }
            if (request.password.length < 6) {
                throw IllegalArgumentException("Password must be at least 6 characters")
            }

            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            if (request.email.isBlank() || request.password.isBlank()) {
                throw IllegalArgumentException("Email and password cannot be empty")
            }

            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val userId = call.getUserId()
                val role = call.getUserRole()

                if (role == "TRAINER") {
                    val profile = authService.getTrainerProfile(userId)
                        ?: throw ResourceNotFoundException("Trainer profile not found")
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    val profile = authService.getMemberProfile(userId)
                        ?: throw ResourceNotFoundException("Member profile not found")
                    call.respond(HttpStatusCode.OK, profile)
                }
            }
        }
    }
}
