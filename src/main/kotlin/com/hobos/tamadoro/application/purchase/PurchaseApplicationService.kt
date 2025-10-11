package com.hobos.tamadoro.application.purchase

import com.hobos.tamadoro.domain.purchase.PurchaseService
import com.hobos.tamadoro.domain.user.SubscriptionType
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PurchaseApplicationService(
    private val purchaseService: PurchaseService
) {
    fun subscriptionPlans() = purchaseService.getSubscriptionPlans()
    fun subscriptionStatus(userId: UUID) = purchaseService.subscriptionStatus(userId)
    fun subscribe(userId: UUID, type: SubscriptionType) = purchaseService.subscribe(userId, type)
    fun cancel(userId: UUID) = purchaseService.cancel(userId)
    fun subscriptionHistory(userId: UUID) = purchaseService.subscriptionHistory(userId)
}


