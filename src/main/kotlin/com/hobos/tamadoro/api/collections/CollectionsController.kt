package com.hobos.tamadoro.api.collections

import com.hobos.tamadoro.application.collections.CollectionsApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class CollectionsController(
    private val collectionsApplicationService: CollectionsApplicationService
) {
    @GetMapping("/backgrounds")
    fun getBackgrounds(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getBackgrounds(userId)))

    @PutMapping("/backgrounds/active")
    fun setActiveBackground(
        @CurrentUserId userId: UUID,
        @RequestBody req: SetActiveRequest
    ) = ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.setActiveBackground(userId, req.id)))

    @PostMapping("/backgrounds/{id}/purchase")
    fun purchaseBackground(@CurrentUserId userId: UUID, @PathVariable id: String) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.purchaseBackground(userId, id)))

    @GetMapping("/music/tracks")
    fun getMusic(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getMusic(userId)))

    @PutMapping("/music/active")
    fun setActiveMusic(
        @CurrentUserId userId: UUID,
        @RequestBody req: SetActiveRequest
    ) = ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.setActiveMusic(userId, req.id)))

    @PostMapping("/music/tracks/{id}/purchase")
    fun purchaseMusic(@CurrentUserId userId: UUID, @PathVariable id: String) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.purchaseMusic(userId, id)))

    @GetMapping("/characters")
    fun getCharacters(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getCharacters(userId)))

    @PutMapping("/characters/active")
    fun setActiveCharacter(
        @CurrentUserId userId: UUID,
        @RequestBody req: SetActiveRequest
    ) = ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.setActiveCharacter(userId, req.id)))

    @PostMapping("/characters/{id}/purchase")
    fun purchaseCharacter(@CurrentUserId userId: UUID, @PathVariable id: String) =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.purchaseCharacter(userId, id)))
}

data class SetActiveRequest(val id: String)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data, null)
        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> =
            ApiResponse(false, null, ErrorResponse(code, message, details))
    }
}

data class ErrorResponse(val code: Int, val message: String, val details: Any? = null)


