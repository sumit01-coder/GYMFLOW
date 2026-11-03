package com.gymflow.server.routes

import com.gymflow.server.model.JoinTrainerRequest
import com.gymflow.server.model.RecordBodyMeasurementRequest
import com.gymflow.server.model.RecordHydrationRequest
import com.gymflow.server.model.RecordWeightRequest
import com.gymflow.server.plugins.ResourceNotFoundException
import com.gymflow.server.plugins.getUserId
import com.gymflow.server.plugins.requireRole
import com.gymflow.server.service.MemberService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.memberRoutes(memberService: MemberService) {
    authenticate("auth-jwt") {
        route("/member") {
            get("/workouts") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val plan = memberService.getActiveWorkoutPlan(memberId)
                if (plan != null) {
                    call.respond(HttpStatusCode.OK, plan)
                } else {
                    call.respond(HttpStatusCode.OK, emptyList<String>())
                }
            }

            post("/join") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val request = call.receive<JoinTrainerRequest>()
                if (request.code.isBlank()) {
                    throw IllegalArgumentException("Invitation code cannot be blank")
                }

                val response = memberService.joinTrainer(memberId, request.code)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/weight") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val request = call.receive<RecordWeightRequest>()
                if (request.preWeightKg <= 0 || request.postWeightKg <= 0) {
                    throw IllegalArgumentException("Weights must be greater than zero")
                }

                val result = memberService.recordWeight(memberId, request)
                call.respond(HttpStatusCode.Created, result)
            }

            get("/weight") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val history = memberService.getWeightHistory(memberId)
                call.respond(HttpStatusCode.OK, history)
            }

            post("/hydration") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val request = call.receive<RecordHydrationRequest>()
                if (request.amountMl <= 0) {
                    throw IllegalArgumentException("Hydration amount must be positive")
                }

                val result = memberService.recordHydration(memberId, request)
                call.respond(HttpStatusCode.Created, result)
            }

            post("/measurements") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val request = call.receive<RecordBodyMeasurementRequest>()
                val result = memberService.recordMeasurements(memberId, request)
                call.respond(HttpStatusCode.Created, result)
            }

            get("/progress") {
                call.requireRole("MEMBER")
                val memberId = call.getUserId()
                val progress = memberService.getProgress(memberId)
                call.respond(HttpStatusCode.OK, progress)
            }
        }
    }
}
