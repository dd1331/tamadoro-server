package com.hobos.tamadoro.api.user

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.user.UpdateUserProfileRequest
import com.hobos.tamadoro.application.user.UserApplicationService
import com.hobos.tamadoro.application.user.UserDto
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for user-related endpoints.
 */
@RestController
@RequestMapping("/user")
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
