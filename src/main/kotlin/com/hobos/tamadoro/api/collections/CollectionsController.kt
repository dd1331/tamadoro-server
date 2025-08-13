package com.hobos.tamadoro.api.collections

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.collections.CollectionsApplicationService
import com.hobos.tamadoro.application.collections.model.BackgroundItem
import com.hobos.tamadoro.application.collections.model.MusicItem
import com.hobos.tamadoro.application.collections.model.TamagotchiItem
import com.hobos.tamadoro.config.CurrentUserId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class CollectionsController(
    private val collectionsApplicationService: CollectionsApplicationService
) {
    @GetMapping("/backgrounds")
    fun getBackgrounds(): ResponseEntity<ApiResponse<List<BackgroundItem>>> =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getBackgrounds()))

    @PutMapping("/backgrounds/active")
    fun setActiveBackground(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: SetActiveRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.setActiveBackground(userId, req.id)))

    @PostMapping("/backgrounds/{id}/purchase")
    fun purchaseBackground(@CurrentUserId userId: UUID, @PathVariable id: String): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.purchaseBackground(userId, id)))

    @GetMapping("/sound/tracks")
    fun getMusic(): ResponseEntity<ApiResponse<List<MusicItem>>> =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getSound()))

    @PutMapping("/sound/active")
    fun setActiveMusic(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: SetActiveRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.setActiveMusic(userId, req.id)))

    @PostMapping("/sound/tracks/{id}/purchase")
    fun purchaseMusic(@CurrentUserId userId: UUID, @PathVariable id: String): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.purchaseMusic(userId, id)))

    @GetMapping("/tamagotchis")
    fun getTamagotchis(): ResponseEntity<ApiResponse<List<TamagotchiItem>>> =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getTamagotchis()))

    @PutMapping("/tamagotchis/active")
    fun setActiveTamagotchi(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: SetActiveRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.setActiveTamagotchi(userId, req.id)))

    @PostMapping("/tamagotchis/{id}/purchase")
    fun purchaseTamagotchi(@CurrentUserId userId: UUID, @PathVariable id: String): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.purchaseTamagotchi(userId, id)))
}

data class SetActiveRequest(@field:NotBlank val id: String)

