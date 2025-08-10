package com.hobos.tamadoro.api.inventory

import com.hobos.tamadoro.application.inventory.InventoryApplicationService
import com.hobos.tamadoro.application.inventory.InventoryDto
import com.hobos.tamadoro.application.inventory.UpdateCoinsRequest
import com.hobos.tamadoro.application.inventory.UpdateGemsRequest
import com.hobos.tamadoro.application.inventory.SetActiveTamagotchiRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for inventory-related endpoints.
 */
@RestController
@RequestMapping("/api/inventory")
class InventoryController(
    private val inventoryApplicationService: InventoryApplicationService
) {
    /**
     * Gets a user's inventory.
     */
    @GetMapping
    fun getInventory(@RequestHeader("User-ID") userId: UUID): ResponseEntity<ApiResponse<InventoryDto>> {
        val inventory = inventoryApplicationService.getInventory(userId)
        return ResponseEntity.ok(ApiResponse.success(inventory))
    }
    
    /**
     * Updates a user's coins.
     */
    @PutMapping("/coins")
    fun updateCoins(
        @RequestHeader("User-ID") userId: UUID,
        @RequestBody request: UpdateCoinsRequest
    ): ResponseEntity<ApiResponse<InventoryDto>> {
        val inventory = inventoryApplicationService.updateCoins(userId, request.amount)
        return ResponseEntity.ok(ApiResponse.success(inventory))
    }
    
    /**
     * Updates a user's gems.
     */
    @PutMapping("/gems")
    fun updateGems(
        @RequestHeader("User-ID") userId: UUID,
        @RequestBody request: UpdateGemsRequest
    ): ResponseEntity<ApiResponse<InventoryDto>> {
        val inventory = inventoryApplicationService.updateGems(userId, request.amount)
        return ResponseEntity.ok(ApiResponse.success(inventory))
    }
    
    /**
     * Sets the active tamagotchi.
     */
    @PutMapping("/active-tamagotchi")
    fun setActiveTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @RequestBody request: SetActiveTamagotchiRequest
    ): ResponseEntity<ApiResponse<InventoryDto>> {
        val inventory = inventoryApplicationService.setActiveTamagotchi(userId, request.tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(inventory))
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