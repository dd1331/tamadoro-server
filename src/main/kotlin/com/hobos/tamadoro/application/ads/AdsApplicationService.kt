package com.hobos.tamadoro.application.ads

import com.hobos.tamadoro.domain.ads.AdsService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdsApplicationService(
    private val adsService: AdsService
) {
    fun logInterstitial(userId: UUID, context: String) {
        adsService.logInterstitial(userId, context)
    }

    fun logBanner(userId: UUID, placement: String) {
        adsService.logBanner(userId, placement)
    }
}


