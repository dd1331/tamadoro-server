package com.hobos.tamadoro.application.purchase

import com.hobos.tamadoro.domain.user.Subscription
import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType

/**
 * DTO for subscription status information
 */
data class SubscriptionStatusDto(
    val type: SubscriptionType,
    val startDate: String,
    val endDate: String?,
    val status: SubscriptionStatus
) {
    companion object {
        fun fromSubscription(subscription: Subscription): SubscriptionStatusDto {
            return SubscriptionStatusDto(
                type = subscription.type,
                startDate = subscription.startDate.toString(),
                endDate = subscription.endDate?.toString(),
                status = subscription.status,
            )
        }
    }
}

/**
 * DTO for subscription history item
 */
data class SubscriptionHistoryItemDto(
    val type: String,
    val startDate: String,
    val endDate: String?,
    val status: String
) {
    companion object {
        fun fromSubscription(subscription: Subscription): SubscriptionHistoryItemDto {
            return SubscriptionHistoryItemDto(
                type = subscription.type.name.lowercase(),
                startDate = subscription.startDate.toString(),
                endDate = subscription.endDate?.toString(),
                status = subscription.status.name.lowercase()
            )
        }
    }
}

/**
 * DTO for coin package information
 */
data class CoinPackageDto(
    val amount: Int,
    val price: Int,
    val bonus: Int
)

/**
 * DTO for gem package information
 */
data class GemPackageDto(
    val amount: Int,
    val price: Int,
    val bonus: Int
)

/**
 * DTO for premium subscription plan
 */
data class PremiumSubscriptionDto(
    val type: SubscriptionType,
    val price: Int,
    val features: List<String>
)

