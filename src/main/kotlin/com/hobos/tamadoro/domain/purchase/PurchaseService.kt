package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
        PremiumSubscription("weekly", 1_500, listOf("No Ads")),
        PremiumSubscription("monthly", 4_900, listOf("No Ads", "Premium Themes")),
        PremiumSubscription("yearly", 39_000, listOf("No Ads", "Premium Themes", "2 months free")),
        PremiumSubscription("unlimited", 99_000, listOf("No Ads", "Premium Themes", "Lifetime access"))
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
        val today = LocalDate.now()
        val sub = user.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }
        return mapOf(
            "type" to sub?.type?.name?.lowercase(),
            "startDate" to sub?.startDate?.toString(),
            "endDate" to sub?.endDate?.toString(),
            "status" to sub?.status?.name?.lowercase()
        )
    }

    fun subscribe(userId: UUID, type: SubscriptionType): Map<String, Any?> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }

        val today = LocalDate.now()
        val active = user.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }

        if (active != null) {
            // If already subscribed, extend the current active subscription
            if (type == SubscriptionType.UNLIMITED) {
                // Convert to unlimited by clearing endDate and updating type
                active.type = SubscriptionType.UNLIMITED
                active.endDate = null
            } else {
                val baseDate = active.endDate?.toLocalDate()?.let { if (!it.isBefore(today)) it else today } ?: today
                val newDate = when (type) {
                    SubscriptionType.WEEKLY -> baseDate.plusWeeks(1)
                    SubscriptionType.MONTHLY -> baseDate.plusMonths(1)
                    SubscriptionType.YEARLY -> baseDate.plusYears(1)
                    SubscriptionType.UNLIMITED -> baseDate // unused
                }
                val newEnd = LocalDateTime.of(newDate, LocalTime.MAX)
                active.type = type
                active.endDate = newEnd
            }
            userRepository.save(user)
            return subscriptionStatus(userId)
        }

        // No active subscription: create a new one starting now
        val startDate = today
        val end: LocalDateTime? = when (type) {
            SubscriptionType.WEEKLY -> LocalDateTime.of(startDate.plusWeeks(1), LocalTime.MAX)
            SubscriptionType.MONTHLY -> LocalDateTime.of(startDate.plusMonths(1), LocalTime.MAX)
            SubscriptionType.YEARLY -> LocalDateTime.of(startDate.plusYears(1), LocalTime.MAX)
            SubscriptionType.UNLIMITED -> null
        }
        user.activatePremium(type, end)
        userRepository.save(user)
        return subscriptionStatus(userId)
    }

    fun cancel(userId: UUID): Map<String, Any?> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        user.cancelPremium()
        userRepository.save(user)
        return subscriptionStatus(userId)
    }

    fun subscriptionHistory(userId: UUID): List<Map<String, Any?>> {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        return user.subscriptions
            .sortedByDescending { it.startDate }
            .map { sub ->
                mapOf(
                    "type" to sub.type.name.lowercase(),
                    "startDate" to sub.startDate.toString(),
                    "endDate" to sub.endDate?.toString(),
                    "status" to sub.status.name.lowercase()
                )
            }
    }
}


