package com.hobos.tamadoro.domain.ads

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class AdsService {
    private val logger = LoggerFactory.getLogger(AdsService::class.java)

    fun logInterstitial(userId: UUID, context: String) {
        logger.info("ads_interstitial userId={} context={} at={}", userId, context, LocalDateTime.now())
    }

    fun logBanner(userId: UUID, placement: String) {
        logger.info("ads_banner userId={} placement={} at={}", userId, placement, LocalDateTime.now())
    }
}


