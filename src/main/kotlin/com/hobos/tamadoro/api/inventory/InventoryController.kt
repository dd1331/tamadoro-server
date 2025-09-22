package com.hobos.tamadoro.api.inventory

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.inventory.InventoryApplicationService
import com.hobos.tamadoro.application.inventory.InventoryDto
import com.hobos.tamadoro.application.inventory.SetActiveTamaRequest
import com.hobos.tamadoro.application.inventory.UpdateCoinsRequest
import com.hobos.tamadoro.application.inventory.UpdateGemsRequest
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/inventory")
class InventoryController(
    private val inventoryApplicationService: InventoryApplicationService
) {
    @GetMapping
    fun getInventory(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<InventoryDto>> {
        val inventory = inventoryApplicationService.getInventory(userId)
        return ResponseEntity.ok(ApiResponse.success(inventory))
    }

    @PutMapping("/coins")
    fun updateCoins(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateCoinsRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        inventoryApplicationService.updateCoins(userId, request.amount)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PutMapping("/gems")
    fun updateGems(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateGemsRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        inventoryApplicationService.updateGems(userId, request.amount)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PutMapping("/active-tama")
    fun setActiveTama(
        @CurrentUserId userId: UUID,
        @RequestBody request: SetActiveTamaRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        inventoryApplicationService.setActiveTama(userId, request.id)
        return ResponseEntity.ok(ApiResponse.success())
    }
}
