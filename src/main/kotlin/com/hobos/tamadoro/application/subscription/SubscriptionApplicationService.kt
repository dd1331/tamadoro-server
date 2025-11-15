package com.hobos.tamadoro.application.subscription

import com.hobos.tamadoro.domain.purchase.PurchaseService
import com.hobos.tamadoro.domain.purchase.SubscribeCommand
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubscriptionApplicationService(
    private val purchaseService: PurchaseService
) {
    fun subscriptionPlans() = purchaseService.getSubscriptionPlans()
    fun subscriptionStatus(userId: UUID) = purchaseService.subscriptionStatus(userId)
    fun subscribe(userId: UUID, command: SubscribeCommand) = purchaseService.subscribe(userId, command)
    fun cancel(userId: UUID) = purchaseService.cancel(userId)
    fun subscriptionHistory(userId: UUID) = purchaseService.subscriptionHistory(userId)
}
