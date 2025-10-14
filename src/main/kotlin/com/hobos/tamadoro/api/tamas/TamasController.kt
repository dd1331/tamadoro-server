package com.hobos.tamadoro.api.tamas

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.tama.CreateTamaRequest
import com.hobos.tamadoro.application.tama.TamaApplicationService
import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.application.tama.UpdateTamaRequest
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/tamas")
class TamasController(
    private val tamaApplicationService: TamaApplicationService
) {
    @GetMapping
    fun getTamas(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<TamaDto>>> {
        val tamas = tamaApplicationService.getTamas(userId)
        println("tamas+" + tamas)
        return ResponseEntity.ok(ApiResponse.success(tamas))
    }

    @PostMapping
    fun createTama(
        @CurrentUserId userId: UUID,
        @RequestBody request: CreateTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.createTama(userId, request)
        return ResponseEntity.created(URI.create("/tamas/${tama.id}")).body(ApiResponse.success(tama))
    }

    @PutMapping("/{tamaId}")
    fun updateTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID,
        @RequestBody request: UpdateTamaRequest
    ): ResponseEntity<ApiResponse<TamaDto>> {
        val tama = tamaApplicationService.updateTama(userId, tamaId, request)
        return ResponseEntity.ok(ApiResponse.success(tama))
    }

    @DeleteMapping("/{tamaId}")
    fun deleteTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.deleteTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/{tamaId}/feed")
    fun feedTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.feedTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/{tamaId}/play")
    fun playWithTama(
        @CurrentUserId userId: UUID,
        @PathVariable tamaId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        tamaApplicationService.playWithTama(userId, tamaId)
        return ResponseEntity.ok(ApiResponse.success())
    }


    data class ExperienceRequest(val amount: Int)
}
