package com.hobos.tamadoro.api.auth

import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AuthResponse
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
    fun authenticateWithApple(@RequestBody request: AppleAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.authenticateWithApple(request)
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response))
    }
    
    /**
     * Refreshes the authentication token.
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * Logs out the user.
     */
    @PostMapping("/logout")
    fun logout(@RequestBody request: LogoutRequest): ResponseEntity<ApiResponse<Unit>> {
        authApplicationService.logout(request.token)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}

/**
 * Request for refreshing authentication token.
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * Request for logging out.
 */
data class LogoutRequest(
    val token: String
)

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorResponse(code, message, details)
            )
        }
    }
}

/**
 * Error response.
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
    val details: Any? = null
) 