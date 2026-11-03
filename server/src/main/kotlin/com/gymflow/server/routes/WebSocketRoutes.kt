package com.gymflow.server.routes

import com.gymflow.server.model.LiveWorkoutEvent
import com.gymflow.server.service.JwtService
import com.gymflow.server.sync.WebSocketManager
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.gymflow.server.routes.WebSocketRoutes")
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun Route.webSocketRoutes() {
    webSocket("/ws/live") {
        val token = call.request.queryParameters["token"]
        val directUserId = call.request.queryParameters["userId"]

        val userId = if (!token.isNullOrBlank()) {
            try {
                val decoded = JwtService.verifier.verify(token)
                decoded.getClaim("userId").asString()
            } catch (e: Exception) {
                logger.warn("WebSocket token verification failed: ${e.message}")
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid or expired token"))
                return@webSocket
            }
        } else if (!directUserId.isNullOrBlank()) {
            directUserId
        } else {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing authentication token or userId"))
            return@webSocket
        }

        WebSocketManager.registerSession(userId, this)
        logger.info("User $userId connected to /ws/live")

        try {
            // Send initial connected acknowledgement
            val connectedAck = json.encodeToString(
                LiveWorkoutEvent(
                    type = "CONNECTED",
                    memberId = userId,
                    memberName = "System",
                    workoutTitle = "Real-time sync established"
                )
            )
            send(Frame.Text(connectedAck))

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val event = json.decodeFromString<LiveWorkoutEvent>(text)
                        logger.info("Received live event '${event.type}' from user $userId")
                        WebSocketManager.broadcastLiveEvent(event)
                    } catch (e: Exception) {
                        logger.debug("Received raw WebSocket message: $text")
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("User $userId disconnected from /ws/live (channel closed)")
        } catch (e: Throwable) {
            logger.warn("WebSocket error for user $userId: ${e.message}")
        } finally {
            WebSocketManager.unregisterSession(userId, this)
            logger.info("Cleaned up WebSocket session for user $userId")
        }
    }
}
