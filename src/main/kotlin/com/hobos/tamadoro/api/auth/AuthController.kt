package com.hobos.tamadoro.api.auth

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.AuthResponse
import com.hobos.tamadoro.application.auth.TokenPair
import com.hobos.tamadoro.config.CurrentUserId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for authentication-related endpoints.
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authApplicationService: AuthApplicationService
) {
    /**
     * Handles Apple Sign-In authentication.
     */
    @PostMapping("/apple")
    fun authenticateWithApple(@Valid @RequestBody request: AppleAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val currentAuthentication = SecurityContextHolder.getContext().authentication
        val currentUserId = runCatching { UUID.fromString(currentAuthentication?.name) }.getOrNull()

        val response = authApplicationService.authenticateWithApple(request, currentUserId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * Refreshes the authentication token.
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<TokenPair>> {
        val response = authApplicationService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * Logs out the user.
     */
    @PostMapping("/logout")
    fun logout(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        authApplicationService.logout(userId)
        return ResponseEntity.ok(ApiResponse.success())
    }
}

/**
 * Request for refreshing authentication token.
 */
data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String
)
