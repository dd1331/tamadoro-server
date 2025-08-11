package com.hobos.tamadoro.api.purchase

import com.hobos.tamadoro.application.purchase.PurchaseApplicationService
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.user.SubscriptionType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api")
class PurchaseController(
    private val purchaseApplicationService: PurchaseApplicationService
) {
    @GetMapping("/purchase/coins")
    fun coinPackages() = ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.coinPackages()))

    @GetMapping("/purchase/gems")
    fun gemPackages() = ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.gemPackages()))

    data class PurchaseRequest(val packageId: String)

    @PostMapping("/purchase/coins")
    fun buyCoins(@CurrentUserId userId: UUID, @RequestBody req: PurchaseRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.buyCoins(userId, req.packageId)))

    @PostMapping("/purchase/gems")
    fun buyGems(@CurrentUserId userId: UUID, @RequestBody req: PurchaseRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.buyGems(userId, req.packageId)))

    @GetMapping("/subscription/plans")
    fun plans() = ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionPlans()))

    @GetMapping("/subscription/status")
    fun status(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionStatus(userId)))

    data class SubscribeRequest(val type: SubscriptionType)

    @PostMapping("/subscription/subscribe")
    fun subscribe(@CurrentUserId userId: UUID, @RequestBody req: SubscribeRequest) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscribe(userId, req.type)))

    @PostMapping("/subscription/cancel")
    fun cancel(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.cancel(userId)))

    @GetMapping("/subscription/history")
    fun history(@CurrentUserId userId: UUID) =
        ResponseEntity.ok(ApiResponse.success(purchaseApplicationService.subscriptionHistory(userId)))
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data, null)
        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> =
            ApiResponse(false, null, ErrorResponse(code, message, details))
    }
}

data class ErrorResponse(val code: Int, val message: String, val details: Any? = null)


