package com.gymflow.server.plugins

import com.gymflow.server.config.ServerConfig
import com.gymflow.server.service.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal

class UnauthorizedException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = ServerConfig.jwtRealm
            verifier(JwtService.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                val role = credential.payload.getClaim("role").asString()
                if (!userId.isNullOrBlank() && !role.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getUserId(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Missing or invalid authentication token")
    return principal.payload.getClaim("userId").asString()
        ?: throw UnauthorizedException("Invalid token payload: missing userId")
}

fun ApplicationCall.getUserRole(): String {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("Missing or invalid authentication token")
    return principal.payload.getClaim("role").asString()
        ?: throw UnauthorizedException("Invalid token payload: missing role")
}

fun ApplicationCall.requireRole(vararg allowedRoles: String) {
    val role = getUserRole()
    if (role !in allowedRoles) {
        throw ForbiddenException("Access denied: required role is ${allowedRoles.joinToString(" or ")}, but user role is $role")
    }
}
