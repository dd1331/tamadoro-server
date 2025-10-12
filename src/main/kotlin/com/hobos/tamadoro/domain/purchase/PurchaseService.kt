package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.UserRepository
import com.hobos.tamadoro.application.purchase.SubscriptionStatusDto
import com.hobos.tamadoro.application.purchase.SubscriptionHistoryItemDto
import com.hobos.tamadoro.application.purchase.PremiumSubscriptionDto
import com.hobos.tamadoro.domain.user.Subscription
import com.hobos.tamadoro.domain.user.SubscriptionRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.NoSuchElementException

data class PremiumSubscription(val type: SubscriptionType, val price: Int, val features: List<String>)

@Service
class PurchaseService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository
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


    fun subscribe(userId: UUID, type: SubscriptionType): SubscriptionStatusDto {
        // TODO: 결제이력 추가
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val sub = createOrExtendSubscription( type, user)
        userRepository.save(user)
        return SubscriptionStatusDto.fromSubscription(sub)
    }

    private fun createOrExtendSubscription(
        type: SubscriptionType,
        user: User
    ): Subscription {
        val active = user.activeSubscription()

        if (active != null) {
            // 기존 구독을 연장
            active.renew(type)
            return active
        }
            // 새로운 구독 생성
        return createSubscription(user, type)
    }

    private fun createSubscription(
        user: User,
        type: SubscriptionType
    ): Subscription {
        val newSubscription = user.activatePremium(type)

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


