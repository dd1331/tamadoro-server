package com.hobos.tamadoro.api.user

import com.hobos.tamadoro.application.user.UserApplicationService
import com.hobos.tamadoro.application.user.UpdateUserProfileRequest
import com.hobos.tamadoro.application.user.UserDto
import org.springframework.http.ResponseEntity
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for user-related endpoints.
 */
@RestController
@RequestMapping("/api/user")
class UserController(
    private val userApplicationService: UserApplicationService
) {
    /**
     * Gets a user's profile.
     */
    @GetMapping("/profile")
    fun getUserProfile(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userApplicationService.getUserProfile(userId)
        return ResponseEntity.ok(ApiResponse.success(user))
    }

    @PutMapping("/profile")
    fun updateUserProfile(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<UserDto>> {
        val user = userApplicationService.updateUserProfile(userId, request)
        return ResponseEntity.ok(ApiResponse.success(user))
    }

}

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
