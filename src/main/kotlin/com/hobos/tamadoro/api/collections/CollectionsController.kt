package com.hobos.tamadoro.api.collections

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.collections.CollectionsApplicationService
import com.hobos.tamadoro.application.collections.model.BackgroundItem
import com.hobos.tamadoro.application.collections.model.MusicItem
import com.hobos.tamadoro.application.collections.model.TamaItem
import com.hobos.tamadoro.application.collections.CollectionsApplicationService.UserCollectionSettingsDto
import com.hobos.tamadoro.config.CurrentUserId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping
class CollectionsController(
    private val collectionsApplicationService: CollectionsApplicationService
) {
    @GetMapping("/settings")
    fun getSettings(
        @CurrentUserId userId: UUID
    ): ResponseEntity<ApiResponse<UserCollectionSettingsDto>> =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getSettings(userId)))

    @PutMapping("/backgrounds/active")
    fun setActiveBackground(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: SetActiveBGRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.setActiveBackground(userId, req.url)))



    // Catalog of characters (tamas) for collection
    @GetMapping("/characters")
    fun getCharacters(): ResponseEntity<ApiResponse<List<TamaItem>>> =
        ResponseEntity.ok(ApiResponse.success(collectionsApplicationService.getTamas()))

    @PutMapping("/tamas/active")
    fun setActiveTama(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody req: SetActiveRequest
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.setActiveTama(userId, req.id)))

    @PostMapping("/tamas/{id}/purchase")
    fun purchaseTama(@CurrentUserId userId: UUID, @PathVariable id: Long): ResponseEntity<ApiResponse<Map<String, Any?>>> =
        ResponseEntity.ok(ApiResponse.success<Map<String, Any?>>(collectionsApplicationService.purchaseTama(userId, id)))
}

data class SetActiveBGRequest(@field:NotBlank val url: String)
data class SetActiveRequest(@field:NotBlank val id: Long)
