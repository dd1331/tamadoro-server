package com.hobos.tamadoro.api.randombox

import com.hobos.tamadoro.application.randombox.RandomBoxApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/random-boxes")
class RandomBoxController(
    private val randomBoxApplicationService: RandomBoxApplicationService
) {
    @GetMapping
    fun list(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(randomBoxApplicationService.list(userId)))

    @PostMapping("/{id}/purchase")
    fun purchase(
        @CurrentUserId userId: UUID,
        @PathVariable id: String
    ) = ResponseEntity.ok(ApiResponse.success(randomBoxApplicationService.purchase(userId, id)))
}

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


