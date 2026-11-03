package com.gymflow.server.sync

import com.gymflow.server.model.LiveWorkoutEvent
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object WebSocketManager {
    private val logger = LoggerFactory.getLogger(WebSocketManager::class.java)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // Map of userId -> Set of active WebSocket sessions
    private val sessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketSession>>()

    fun registerSession(userId: String, session: DefaultWebSocketSession) {
        val userSessions = sessions.computeIfAbsent(userId) {
            Collections.synchronizedSet(LinkedHashSet())
        }
        userSessions.add(session)
        logger.info("WebSocket registered for user: $userId (Active sessions: ${userSessions.size})")
    }

    fun unregisterSession(userId: String, session: DefaultWebSocketSession) {
        sessions[userId]?.let { userSessions ->
            userSessions.remove(session)
            if (userSessions.isEmpty()) {
                sessions.remove(userId)
            }
        }
        logger.info("WebSocket unregistered for user: $userId")
    }

    suspend fun broadcastToUser(userId: String, message: String) = withContext(Dispatchers.IO) {
        val userSessions = sessions[userId] ?: return@withContext
        val deadSessions = mutableListOf<DefaultWebSocketSession>()

        userSessions.toList().forEach { session ->
            try {
                session.send(Frame.Text(message))
            } catch (e: Exception) {
                logger.warn("Failed to send WebSocket frame to user $userId: ${e.message}")
                deadSessions.add(session)
            }
        }

        deadSessions.forEach { unregisterSession(userId, it) }
    }

    suspend fun broadcastToTrainer(trainerId: String, event: LiveWorkoutEvent) {
        val message = json.encodeToString(event)
        broadcastToUser(trainerId, message)
    }

    suspend fun broadcastLiveEvent(event: LiveWorkoutEvent) {
        val message = json.encodeToString(event)
        // If event specifies trainerId, broadcast directly to that trainer
        event.trainerId?.let { trainerId ->
            broadcastToUser(trainerId, message)
        }
        // Also echo back to the member's other active devices
        broadcastToUser(event.memberId, message)
    }

    suspend fun broadcastToAll(message: String) = withContext(Dispatchers.IO) {
        sessions.forEach { (userId, sessionList) ->
            sessionList.toList().forEach { session ->
                try {
                    session.send(Frame.Text(message))
                } catch (e: Exception) {
                    unregisterSession(userId, session)
                }
            }
        }
    }

    fun getActiveUserCount(): Int = sessions.size

    fun isUserConnected(userId: String): Boolean = sessions[userId]?.isNotEmpty() == true
}
