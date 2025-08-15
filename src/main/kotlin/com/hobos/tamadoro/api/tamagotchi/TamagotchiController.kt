package com.hobos.tamadoro.api.tama

import com.hobos.tamadoro.application.tama.TamaApplicationService
import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.application.tama.CreateTamaRequest
import com.hobos.tamadoro.application.tama.UpdateTamaRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for tama-related endpoints.
 */
@RestController
@RequestMapping("/api/tamas")
class TamaController(
    private val tamaApplicationService: TamaApplicationService
) {
    /**
     * Gets all tamas for a user.
     */
//    @GetMapping
//    fun getTamas(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<TamaDto>>> {
//        val tamas = tamaApplicationService.getTamas(userId)
//        return ResponseEntity.ok(ApiResponse.success(tamas))
//    }
//
    /**
     * Gets a specific tama by ID.
     */
    @GetMapping("/{tamaId}")
    fun getTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.getTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }
    
    /**
     * Creates a new tama.
     */
    @PostMapping
    fun createTama(
        @CurrentUserId userId: UUID,
        @RequestBody request: CreateTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.createTama(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(tama))
    }
    
    /**
     * Updates a tama.
     */
    @PutMapping("/{tamaId}")
    fun updateTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID,
        @RequestBody request: UpdateTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.updateTama(userId, tamaId, request)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }
    
    /**
     * Deletes a tama.
     */
    @DeleteMapping("/{tamaId}")
    fun deleteTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.deleteTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
    
    /**
     * Feeds a tama.
     */
    @PostMapping("/{tamaId}/feed")
    fun feedTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.feedTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }
    
    /**
     * Plays with a tama.
     */
    @PostMapping("/{tamaId}/play")
    fun playWithTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.playWithTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }
    
    /**
     * Sets a tama as active.
     */
    @PostMapping("/{tamaId}/activate")
    fun activateTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.activateTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    /**
     * Adds experience to a tama.
     */
    @PostMapping("/{tamaId}/experience")
    fun addExperience(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID,
        @RequestBody req: ExperienceRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val dto = tamaApplicationService.addExperience(userId, tamaId, req.amount)
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