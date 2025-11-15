package com.hobos.tamadoro.api.subscription

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.subscription.SubscriptionApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.purchase.PurchasePlatform
import com.hobos.tamadoro.domain.purchase.PurchaseRecord
import com.hobos.tamadoro.domain.purchase.SubscribeCommand
import com.hobos.tamadoro.domain.user.SubscriptionType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping
class SubscriptionController(
    private val purchaseApplicationService: SubscriptionApplicationService
) {


    data class PurchaseRequest(@field:NotBlank val packageId: String)


    @GetMapping("/subscription/plans")
    fun plans() = ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionPlans()))

    @GetMapping("/subscription/status")
    fun status(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionStatus(userId)))

    data class SubscribeRequest(
        val type: SubscriptionType,
        val platform: PurchasePlatform,
        @field:NotBlank
        val productId: String,
        @field:NotBlank
        val transactionId: String,
        val purchaseToken: String? = null,
        val receiptData: String? = null,
        val purchasedAt: LocalDateTime? = null,
        val expiresAt: LocalDateTime? = null,
        val priceAmount: Long? = null,
    ) {
        fun toCommand(): SubscribeCommand = SubscribeCommand(
            type = type,
            purchase = PurchaseRecord(
                platform = platform,
                productId = productId,
                transactionId = transactionId,
                purchaseToken = purchaseToken,
                receiptData = receiptData,
                purchasedAt = purchasedAt,
                expiresAt = expiresAt,
                priceAmount = priceAmount,
            )
        )
    }

    @PostMapping("/subscription/subscribe")
    fun subscribe(@CurrentUserId userId: UUID, @Valid @RequestBody req: SubscribeRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscribe(userId, req.toCommand())))

    // Alias to match spec: POST /subscription
    @PostMapping("/subscription")
    fun subscribeAlias(@CurrentUserId userId: UUID, @Valid @RequestBody req: SubscribeRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscribe(userId, req.toCommand())))

    @PostMapping("/subscription/cancel")
    fun cancel(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.cancel(userId)))

    @GetMapping("/subscription/history")
    fun history(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionHistory(userId)))
}
