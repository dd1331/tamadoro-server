package com.hobos.tamadoro.api.tamagotchi

import com.hobos.tamadoro.application.tamagotchi.TamagotchiApplicationService
import com.hobos.tamadoro.application.tamagotchi.TamagotchiDto
import com.hobos.tamadoro.application.tamagotchi.CreateTamagotchiRequest
import com.hobos.tamadoro.application.tamagotchi.UpdateTamagotchiRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for tamagotchi-related endpoints.
 */
@RestController
@RequestMapping("/api/tamagotchis")
class TamagotchiController(
    private val tamagotchiApplicationService: TamagotchiApplicationService
) {
    /**
     * Gets all tamagotchis for a user.
     */
    @GetMapping
    fun getTamagotchis(@RequestHeader("User-ID") userId: UUID): ResponseEntity<ApiResponse<List<TamagotchiDto>>> {
        val tamagotchis = tamagotchiApplicationService.getTamagotchis(userId)
        return ResponseEntity.ok(ApiResponse.success(tamagotchis))
    }
    
    /**
     * Gets a specific tamagotchi by ID.
     */
    @GetMapping("/{tamagotchiId}")
    fun getTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val tamagotchi = tamagotchiApplicationService.getTamagotchi(userId, tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(tamagotchi))
    }
    
    /**
     * Creates a new tamagotchi.
     */
    @PostMapping
    fun createTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @RequestBody request: CreateTamagotchiRequest
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val tamagotchi = tamagotchiApplicationService.createTamagotchi(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tamagotchi))
    }
    
    /**
     * Updates a tamagotchi.
     */
    @PutMapping("/{tamagotchiId}")
    fun updateTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID,
        @RequestBody request: UpdateTamagotchiRequest
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val tamagotchi = tamagotchiApplicationService.updateTamagotchi(userId, tamagotchiId, request)
        return ResponseEntity.ok(ApiResponse.success(tamagotchi))
    }
    
    /**
     * Deletes a tamagotchi.
     */
    @DeleteMapping("/{tamagotchiId}")
    fun deleteTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamagotchiApplicationService.deleteTamagotchi(userId, tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
    
    /**
     * Feeds a tamagotchi.
     */
    @PostMapping("/{tamagotchiId}/feed")
    fun feedTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val tamagotchi = tamagotchiApplicationService.feedTamagotchi(userId, tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(tamagotchi))
    }
    
    /**
     * Plays with a tamagotchi.
     */
    @PostMapping("/{tamagotchiId}/play")
    fun playWithTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val tamagotchi = tamagotchiApplicationService.playWithTamagotchi(userId, tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(tamagotchi))
    }
    
    /**
     * Sets a tamagotchi as active.
     */
    @PostMapping("/{tamagotchiId}/activate")
    fun activateTamagotchi(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamagotchiApplicationService.activateTamagotchi(userId, tamagotchiId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    /**
     * Adds experience to a tamagotchi.
     */
    @PostMapping("/{tamagotchiId}/experience")
    fun addExperience(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable tamagotchiId: UUID,
        @RequestBody req: ExperienceRequest
    ): ResponseEntity<ApiResponse<TamagotchiDto>> {
        val dto = tamagotchiApplicationService.addExperience(userId, tamagotchiId, req.amount)
        return ResponseEntity.ok(ApiResponse.success(dto))
    }

    data class ExperienceRequest(val amount: Int)
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