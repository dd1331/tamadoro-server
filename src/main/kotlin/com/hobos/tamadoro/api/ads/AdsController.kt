package com.hobos.tamadoro.api.ads

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.ads.AdsApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/ads")
class AdsController(
    private val adsApplicationService: AdsApplicationService
) {
    @PostMapping("/interstitial")
    fun logInterstitial(
        @CurrentUserId userId: UUID,
        @RequestBody request: InterstitialRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        adsApplicationService.logInterstitial(userId, request.context)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/banner")
    fun logBanner(
        @CurrentUserId userId: UUID,
        @RequestBody request: BannerRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        adsApplicationService.logBanner(userId, request.placement)
        return ResponseEntity.ok(ApiResponse.success())
    }
}

data class InterstitialRequest(val context: String)
data class BannerRequest(val placement: String)
