package com.gymflow.server.routes

import com.gymflow.server.model.CompleteWorkoutRequest
import com.gymflow.server.model.LogSetRequest
import com.gymflow.server.model.StartWorkoutRequest
import com.gymflow.server.plugins.ResourceNotFoundException
import com.gymflow.server.plugins.getUserId
import com.gymflow.server.service.WorkoutService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.workoutRoutes(workoutService: WorkoutService) {
    authenticate("auth-jwt") {
        route("/workout") {
            get("/exercises") {
                val exercises = workoutService.getAllExercises()
                call.respond(HttpStatusCode.OK, exercises)
            }

            post("/sessions") {
                val memberId = call.getUserId()
                val request = call.receive<StartWorkoutRequest>()
                if (request.workoutTitle.isBlank()) {
                    throw IllegalArgumentException("Workout title cannot be blank")
                }

                val session = workoutService.startSession(memberId, request)
                call.respond(HttpStatusCode.Created, session)
            }

            get("/sessions/{id}") {
                val sessionId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing session ID")

                val session = workoutService.getSessionById(sessionId)
                    ?: throw ResourceNotFoundException("Workout session not found")
                call.respond(HttpStatusCode.OK, session)
            }

            get("/sessions/{id}/sets") {
                val sessionId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing session ID")

                val sets = workoutService.getSetsForSession(sessionId)
                call.respond(HttpStatusCode.OK, sets)
            }

            post("/sessions/{id}/sets") {
                val sessionId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing session ID")
                val request = call.receive<LogSetRequest>()

                if (request.actualReps < 0 || request.actualWeightKg < 0) {
                    throw IllegalArgumentException("Reps and weight must not be negative")
                }

                val setLog = workoutService.logSet(sessionId, request)
                call.respond(HttpStatusCode.Created, setLog)
            }

            post("/sessions/{id}/complete") {
                val sessionId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing session ID")
                val request = call.receive<CompleteWorkoutRequest>()

                val session = workoutService.completeSession(sessionId, request)
                call.respond(HttpStatusCode.OK, session)
            }
        }
    }
}
