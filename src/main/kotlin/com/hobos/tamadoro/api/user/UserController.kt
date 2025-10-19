package com.hobos.tamadoro.api.user

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.tama.OwnTamaRequest
import com.hobos.tamadoro.application.tama.TamaApplicationService
import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.application.user.UpdateUserProfileRequest
import com.hobos.tamadoro.application.user.UserApplicationService
import com.hobos.tamadoro.application.user.UserDto
import com.hobos.tamadoro.application.user.UserRankDto
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * REST controller for user-related endpoints.
 */
@RestController
@RequestMapping("/users")
class UserController(
    private val userApplicationService: UserApplicationService,
    private val tamaApplicationService: TamaApplicationService,
) {

    @PostMapping("/me/tamas")
    fun ownTama(
        @CurrentUserId userId: UUID,
        @RequestBody request: OwnTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.ownTama(userId, request)
        return ResponseEntity.created(URI.create("/tamas/${tama.id}")).body(ApiResponse.success(tama))
    }

    @PutMapping("/me/tamas/{tamaId}")
    fun activateTama(@CurrentUserId userId: UUID, @PathVariable tamaId: Long,) {
        tamaApplicationService.activateTama(userId, tamaId)

    }

    /**
     * Gets a user's profile.
     */
    @GetMapping("/profile")
    fun getUserProfile(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userApplicationService.getUserProfile(userId)
        println("@@@@@@@"+ user)
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

    @GetMapping("/me/ranking")
    fun getMyRanking(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<UserRankDto>> {
        val userRank = userApplicationService.getMyRanking(userId)
        return ResponseEntity.ok(ApiResponse.success(userRank))
    }

    @GetMapping("/me/ranking/groups")
    fun getMyGroupRanking(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<UserRankDto>> {
        val userRank = userApplicationService.getMyRankingByUser(userId)
        return ResponseEntity.ok(ApiResponse.success(userRank))
    }
}
