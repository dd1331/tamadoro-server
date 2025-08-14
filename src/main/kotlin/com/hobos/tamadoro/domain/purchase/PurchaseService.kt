package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.user.Subscription
import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import com.hobos.tamadoro.application.purchase.SubscriptionStatusDto
import com.hobos.tamadoro.application.purchase.SubscriptionHistoryItemDto
import com.hobos.tamadoro.application.purchase.CoinPackageDto
import com.hobos.tamadoro.application.purchase.GemPackageDto
import com.hobos.tamadoro.application.purchase.PremiumSubscriptionDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import java.util.NoSuchElementException

data class CoinPackage(val amount: Int, val price: Int, val bonus: Int)
data class GemPackage(val amount: Int, val price: Int, val bonus: Int)
data class PremiumSubscription(val type: String, val price: Int, val features: List<String>)

@Service
class PurchaseService(
    private val userInventoryRepository: UserInventoryRepository,
    private val userRepository: UserRepository
) {
    private val coinPackages = listOf(
        CoinPackage(100, 1_000, 0),
        CoinPackage(550, 5_000, 50),
        CoinPackage(1200, 10_000, 200)
    )

    private val gemPackages = listOf(
        GemPackage(10, 2_000, 0),
        GemPackage(55, 10_000, 5),
        GemPackage(120, 20_000, 20)
    )

    private val subs = listOf(
        PremiumSubscription("weekly", 1_500, listOf("No Ads")),
        PremiumSubscription("monthly", 4_900, listOf("No Ads", "Premium Themes")),
        PremiumSubscription("yearly", 39_000, listOf("No Ads", "Premium Themes", "2 months free")),
        PremiumSubscription("unlimited", 99_000, listOf("No Ads", "Premium Themes", "Lifetime access"))
    )

    fun coinPackages(): List<CoinPackageDto> = coinPackages.map { 
        CoinPackageDto(it.amount, it.price, it.bonus) 
    }
    
    fun gemPackages(): List<GemPackageDto> = gemPackages.map { 
        GemPackageDto(it.amount, it.price, it.bonus) 
    }

    fun buyCoins(userId: UUID, packageId: String): Map<String, Any> {
        val pkg = coinPackages.getOrNull(packageId.toIntOrNull() ?: -1)
            ?: throw IllegalArgumentException("invalid package id")
        val inv = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Inventory not found") }
        inv.addCoins(pkg.amount + pkg.bonus)
        userInventoryRepository.save(inv)
        return mapOf("coins" to inv.coins)
    }

    fun buyGems(userId: UUID, packageId: String): Map<String, Any> {
        val pkg = gemPackages.getOrNull(packageId.toIntOrNull() ?: -1)
            ?: throw IllegalArgumentException("invalid package id")
        val inv = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Inventory not found") }
        inv.addGems(pkg.amount + pkg.bonus)
        userInventoryRepository.save(inv)
        return mapOf("gems" to inv.gems)
    }

    fun subscriptionPlans(): List<PremiumSubscriptionDto> = subs.map { 
        PremiumSubscriptionDto(it.type, it.price, it.features) 
    }

    fun subscriptionStatus(userId: UUID): SubscriptionStatusDto? {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val today = LocalDate.now()
        val sub = user.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }
        return sub?.let { SubscriptionStatusDto.fromSubscription(it) }
    }

    fun subscribe(userId: UUID, type: SubscriptionType): SubscriptionStatusDto {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }

        val today = LocalDate.now()
        val active = user.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }

        if (active != null) {
            // 기존 구독을 연장
            active.update(today, type)
            userRepository.save(user)
            return SubscriptionStatusDto.fromSubscription(active)
        } else {
            // 새로운 구독 생성
            user.activatePremium(type)
            userRepository.save(user)
            // activatePremium 후 가장 최근 구독을 찾음
            val newSubscription = user.subscriptions
                .filter { it.status == SubscriptionStatus.ACTIVE }
                .maxByOrNull { it.startDate }
                ?: throw IllegalStateException("Failed to create subscription")
            return SubscriptionStatusDto.fromSubscription(newSubscription)
        }
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


