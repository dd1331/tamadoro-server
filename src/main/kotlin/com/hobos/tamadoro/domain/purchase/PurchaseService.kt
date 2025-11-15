package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.UserRepository
import com.hobos.tamadoro.application.subscription.SubscriptionStatusDto
import com.hobos.tamadoro.application.subscription.SubscriptionHistoryItemDto
import com.hobos.tamadoro.application.subscription.PremiumSubscriptionDto
import com.hobos.tamadoro.domain.user.Subscription
import com.hobos.tamadoro.domain.user.SubscriptionRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import java.util.NoSuchElementException

data class PremiumSubscription(val type: SubscriptionType, val price: Int, val features: List<String>)

data class SubscribeCommand(
    val type: SubscriptionType,
    val purchase: PurchaseRecord
)

data class PurchaseRecord(
    val platform: PurchasePlatform,
    val productId: String,
    val transactionId: String,
    val purchaseToken: String?,
    val receiptData: String?,
    val purchasedAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val priceAmount: Long?,
)

@Service
class PurchaseService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val inAppPurchaseRepository: InAppPurchaseRepository
) {




    private val subs = listOf(
        PremiumSubscription(SubscriptionType.TRIAL, 0, listOf("No Ads", "Premium Themes", "Lifetime access")),
        PremiumSubscription(SubscriptionType.WEEKLY, 1_500, listOf("No Ads")),
        PremiumSubscription(SubscriptionType.MONTHLY, 4_900, listOf("No Ads", "Premium Themes")),
        PremiumSubscription(SubscriptionType.YEARLY, 39_000, listOf("No Ads", "Premium Themes", "2 months free")),
        PremiumSubscription(SubscriptionType.UNLIMITED, 99_000, listOf("No Ads", "Premium Themes", "Lifetime access"))
    )



    fun getSubscriptionPlans(): List<PremiumSubscriptionDto> = subs.map {
        PremiumSubscriptionDto(it.type, it.price, it.features) 
    }

    fun subscriptionStatus(userId: UUID): SubscriptionStatusDto? {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val sub = user.activeSubscription()
        return sub?.let { SubscriptionStatusDto.fromSubscription(it) }
    }


    fun subscribe(userId: UUID, command: SubscribeCommand): SubscriptionStatusDto {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val purchaseDetails = command.purchase

        if (inAppPurchaseRepository.existsByTransactionId(purchaseDetails.transactionId)) {
            throw IllegalArgumentException("Purchase already processed")
        }
        val purchaseEntity = InAppPurchase(
            user = user,
            platform = purchaseDetails.platform,
            productId = purchaseDetails.productId,
            transactionId = purchaseDetails.transactionId,
            purchaseToken = purchaseDetails.purchaseToken,
            receiptData = purchaseDetails.receiptData,
            purchasedAt = purchaseDetails.purchasedAt ?: LocalDateTime.now(),
            expiresAt = purchaseDetails.expiresAt,
            priceAmount = purchaseDetails.priceAmount,
        )
        inAppPurchaseRepository.save(purchaseEntity)

        val sub = createOrExtendSubscription(command.type, user, purchaseDetails.purchasedAt)
        userRepository.save(user)
        return SubscriptionStatusDto.fromSubscription(sub)
    }

    private fun createOrExtendSubscription(
        type: SubscriptionType,
        user: User,
        purchasedAt: LocalDateTime?
    ): Subscription {
        val active = user.activeSubscription()

        if (active != null) {
            // 기존 구독을 연장
            active.renew(type)
            return active
        }
            // 새로운 구독 생성
        return createSubscription(user, type, purchasedAt)
    }

    private fun createSubscription(
        user: User,
        type: SubscriptionType,
        purchasedAt: LocalDateTime?
    ): Subscription {
        val newSubscription = user.startSubscription(type, purchasedAt ?: LocalDateTime.now())

        return newSubscription
    }


    fun cancel(userId: UUID): SubscriptionStatusDto? {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        user.cancelPremium()
        userRepository.save(user)
        return subscriptionStatus(userId)
    }

    fun subscriptionHistory(userId: UUID): List<SubscriptionHistoryItemDto> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        return user.subscriptions
            .sortedByDescending { it.startDate }
            .map { SubscriptionHistoryItemDto.fromSubscription(it) }
    }
}
