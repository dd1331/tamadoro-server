package com.hobos.tamadoro.api.auth

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.AuthResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller that issues guest authentication credentials.
 */
@RestController
@RequestMapping("/guests")
class GuestAuthController(
    private val authApplicationService: AuthApplicationService
) {
    /**
     * Creates a guest account and returns access credentials.
     */
    @PostMapping
    fun loginAsGuest(): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.loginAsGuest()
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
