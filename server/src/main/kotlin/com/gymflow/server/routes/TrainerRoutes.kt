package com.gymflow.server.routes

import com.gymflow.server.model.CreatePlanRequest
import com.gymflow.server.model.SimpleResponse
import com.gymflow.server.plugins.ResourceNotFoundException
import com.gymflow.server.plugins.getUserId
import com.gymflow.server.plugins.requireRole
import com.gymflow.server.service.TrainerService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.trainerRoutes(trainerService: TrainerService) {
    authenticate("auth-jwt") {
        route("/trainer") {
            get("/members") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val members = trainerService.getMembers(trainerId)
                call.respond(HttpStatusCode.OK, members)
            }

            get("/invitations") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val invitations = trainerService.getInvitations(trainerId)
                call.respond(HttpStatusCode.OK, invitations)
            }

            post("/invitations") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val invitation = trainerService.generateInvitationCode(trainerId)
                call.respond(HttpStatusCode.Created, invitation)
            }

            post("/requests/{id}/approve") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val requestId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing connection request ID")

                val approved = trainerService.approveRequest(trainerId, requestId)
                if (!approved) {
                    throw ResourceNotFoundException("Connection request not found or not belonging to you")
                }
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Member connection approved successfully"))
            }

            get("/plans") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val plans = trainerService.getPlansByTrainer(trainerId)
                call.respond(HttpStatusCode.OK, plans)
            }

            post("/plans") {
                call.requireRole("TRAINER")
                val trainerId = call.getUserId()
                val request = call.receive<CreatePlanRequest>()
                if (request.name.isBlank()) {
                    throw IllegalArgumentException("Plan name cannot be blank")
                }
                if (request.memberId.isBlank()) {
                    throw IllegalArgumentException("Target memberId cannot be blank")
                }

                val plan = trainerService.createWorkoutPlan(trainerId, request)
                call.respond(HttpStatusCode.Created, plan)
            }

            get("/plans/{id}") {
                call.requireRole("TRAINER")
                val planId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Missing plan ID")

                val plan = trainerService.getPlanById(planId)
                    ?: throw ResourceNotFoundException("Workout plan not found")
                call.respond(HttpStatusCode.OK, plan)
            }
        }
    }
}
