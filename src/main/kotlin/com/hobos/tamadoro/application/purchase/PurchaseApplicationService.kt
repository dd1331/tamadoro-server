package com.hobos.tamadoro.application.purchase

import com.hobos.tamadoro.domain.purchase.PurchaseService
import com.hobos.tamadoro.domain.user.SubscriptionType
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PurchaseApplicationService(
    private val purchaseService: PurchaseService
) {
    fun coinPackages() = purchaseService.coinPackages()
    fun gemPackages() = purchaseService.gemPackages()
    fun buyCoins(userId: UUID, packageId: String) = purchaseService.buyCoins(userId, packageId)
    fun buyGems(userId: UUID, packageId: String) = purchaseService.buyGems(userId, packageId)
    fun subscriptionPlans() = purchaseService.subscriptionPlans()
    fun subscriptionStatus(userId: UUID) = purchaseService.subscriptionStatus(userId)
    fun subscribe(userId: UUID, type: SubscriptionType) = purchaseService.subscribe(userId, type)
    fun cancel(userId: UUID) = purchaseService.cancel(userId)
    fun subscriptionHistory(userId: UUID) = purchaseService.subscriptionHistory(userId)
}


