package com.hobos.tamadoro.api.auth

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AuthResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authApplicationService: AuthApplicationService
) {
    /**
     * Handles Apple Sign-In authentication.
     */
    @PostMapping("/apple")
    fun authenticateWithApple(@Valid @RequestBody request: AppleAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.authenticateWithApple(request)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response))
    }
    
    /**
     * Refreshes the authentication token.
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * Logs out the user.
     */
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<ApiResponse<Unit>> {
        authApplicationService.logout(request.token)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}

/**
 * Request for refreshing authentication token.
 */
data class RefreshTokenRequest(
    @field:jakarta.validation.constraints.NotBlank
    val refreshToken: String
)

/**
 * Request for logging out.
 */
data class LogoutRequest(
    @field:jakarta.validation.constraints.NotBlank
    val token: String
) 