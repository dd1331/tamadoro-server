package com.hobos.tamadoro.api.purchase

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.purchase.PurchaseApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.user.SubscriptionType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping
class PurchaseController(
    private val purchaseApplicationService: PurchaseApplicationService
) {


    data class PurchaseRequest(@field:NotBlank val packageId: String)


    @GetMapping("/subscription/plans")
    fun plans() = ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionPlans()))

    @GetMapping("/subscription/status")
    fun status(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionStatus(userId)))

    data class SubscribeRequest(val type: SubscriptionType)

    @PostMapping("/subscription/subscribe")
    fun subscribe(@CurrentUserId userId: UUID, @RequestBody req: SubscribeRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscribe(userId, req.type)))

    // Alias to match spec: POST /subscription
    @PostMapping("/subscription")
    fun subscribeAlias(@CurrentUserId userId: UUID, @RequestBody req: SubscribeRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscribe(userId, req.type)))

    @PostMapping("/subscription/cancel")
    fun cancel(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.cancel(userId)))

    @GetMapping("/subscription/history")
    fun history(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionHistory(userId)))
}
