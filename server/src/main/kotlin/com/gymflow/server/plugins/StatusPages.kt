package com.gymflow.server.plugins

import com.gymflow.server.model.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("com.gymflow.server.StatusPages")

    install(StatusPages) {
        exception<UnauthorizedException> { call, cause ->
            logger.warn("Unauthorized request: ${cause.message}")
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    error = "UNAUTHORIZED",
                    message = cause.message ?: "Authentication required",
                    status = HttpStatusCode.Unauthorized.value
                )
            )
        }

        exception<ForbiddenException> { call, cause ->
            logger.warn("Forbidden request: ${cause.message}")
            call.respond(
                HttpStatusCode.Forbidden,
                ErrorResponse(
                    error = "FORBIDDEN",
                    message = cause.message ?: "Insufficient permissions",
                    status = HttpStatusCode.Forbidden.value
                )
            )
        }

        exception<ResourceNotFoundException> { call, cause ->
            logger.info("Resource not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "NOT_FOUND",
                    message = cause.message ?: "Requested resource not found",
                    status = HttpStatusCode.NotFound.value
                )
            )
        }

        exception<NoSuchElementException> { call, cause ->
            logger.info("Not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "NOT_FOUND",
                    message = cause.message ?: "Resource not found",
                    status = HttpStatusCode.NotFound.value
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Bad request (IllegalArgument): ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "BAD_REQUEST",
                    message = cause.message ?: "Invalid request parameters",
                    status = HttpStatusCode.BadRequest.value
                )
            )
        }

        exception<BadRequestException> { call, cause ->
            logger.warn("Bad request (Ktor): ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = "BAD_REQUEST",
                    message = cause.message ?: "Malformed payload",
                    status = HttpStatusCode.BadRequest.value
                )
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled internal server error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "INTERNAL_SERVER_ERROR",
                    message = cause.message ?: "An unexpected error occurred",
                    status = HttpStatusCode.InternalServerError.value
                )
            )
        }
    }
}
