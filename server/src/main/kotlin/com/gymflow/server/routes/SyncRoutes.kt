package com.gymflow.server.routes

import com.gymflow.server.model.SimpleResponse
import com.gymflow.server.model.SyncPushRequest
import com.gymflow.server.plugins.getUserId
import com.gymflow.server.service.SyncService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.syncRoutes(syncService: SyncService) {
    authenticate("auth-jwt") {
        route("/sync") {
            get("/changes") {
                val userId = call.getUserId()
                val since = call.request.queryParameters["since"]?.toLongOrNull() ?: 0L
                val response = syncService.pullChanges(userId, since)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/push") {
                val userId = call.getUserId()
                val request = call.receive<SyncPushRequest>()
                val syncedCount = syncService.pushChanges(userId, request.changes)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, "Successfully pushed $syncedCount sync records")
                )
            }
        }
    }
}
