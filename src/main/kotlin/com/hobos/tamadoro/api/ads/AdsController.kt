package com.hobos.tamadoro.api.ads

import com.hobos.tamadoro.application.ads.AdsApplicationService
import org.springframework.http.ResponseEntity
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/ads")
class AdsController(
    private val adsApplicationService: AdsApplicationService
) {
    @PostMapping("/interstitial")
    fun logInterstitial(
        @CurrentUserId userId: UUID,
        @RequestBody request: InterstitialRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        adsApplicationService.logInterstitial(userId, request.context)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @PostMapping("/banner")
    fun logBanner(
        @CurrentUserId userId: UUID,
        @RequestBody request: BannerRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        adsApplicationService.logBanner(userId, request.placement)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}

data class InterstitialRequest(val context: String)
data class BannerRequest(val placement: String)

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


