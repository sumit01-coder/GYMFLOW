package com.gymflow.core.network

import android.util.Log
import com.gymflow.core.security.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorClientFactory @Inject constructor(
    private val tokenManager: TokenManager
) {
    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
        private const val TAG = "KtorClient"
    }

    fun create(
        baseUrl: String = DEFAULT_BASE_URL,
        enableLogging: Boolean = true
    ): HttpClient {
        val jsonConfig = Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(jsonConfig)
            }

            install(WebSockets)

            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 20_000
            }

            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.INFO
                }
            }

            defaultRequest {
                // Ensure base URL ends with / if not present
                val formattedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                url(formattedBase)
                contentType(ContentType.Application.Json)

                val token = tokenManager.getTokenSync()
                if (!token.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }
}
