package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

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
        PremiumSubscription("monthly", 4_900, listOf("No Ads", "Premium Themes")),
        PremiumSubscription("yearly", 39_000, listOf("No Ads", "Premium Themes", "2 months free"))
    )

    fun coinPackages(): List<CoinPackage> = coinPackages
    fun gemPackages(): List<GemPackage> = gemPackages

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

    fun subscriptionPlans(): List<PremiumSubscription> = subs

    fun subscriptionStatus(userId: UUID): Map<String, Any?> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val sub = user.subscription
        return mapOf(
            "type" to sub?.type?.name?.lowercase(),
            "startDate" to sub?.startDate?.toString(),
            "endDate" to sub?.endDate?.toString(),
            "status" to sub?.status?.name?.lowercase()
        )
    }

    fun subscribe(userId: UUID, type: String): Map<String, Any?> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val subType = SubscriptionType.valueOf(type.uppercase())
        val end = if (subType == SubscriptionType.MONTHLY) LocalDateTime.now().plusMonths(1) else LocalDateTime.now().plusYears(1)
        user.activatePremium(subType, end)
        userRepository.save(user)
        return subscriptionStatus(userId)
    }

    fun cancel(userId: UUID): Map<String, Any?> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        user.cancelPremium()
        user.subscription?.status = SubscriptionStatus.CANCELLED
        userRepository.save(user)
        return subscriptionStatus(userId)
    }
}


