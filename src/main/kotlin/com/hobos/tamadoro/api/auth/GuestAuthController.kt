package com.hobos.tamadoro.api.auth

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.AuthResponse
import com.hobos.tamadoro.application.auth.GuestLoginRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    fun loginAsGuest(
        @Valid @RequestBody request: GuestLoginHttpRequest
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authApplicationService.loginAsGuest(
            GuestLoginRequest(countryCode = request.countryCode)
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}

data class GuestLoginHttpRequest(
//    @field:NotBlank
    val countryCode: String
)
