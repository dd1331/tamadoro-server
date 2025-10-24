package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a processed in-app purchase transaction.
 */
@Entity
@Table(
    name = "in_app_purchases",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_in_app_purchase_transaction", columnNames = ["transaction_id"])
    ]
)
class InAppPurchase(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    val platform: PurchasePlatform,

    @Column(name = "product_id", nullable = false)
    val productId: String,

    @Column(name = "transaction_id", nullable = false)
    val transactionId: String,

    @Lob
    @Column(name = "receipt_data", columnDefinition = "text" )
    val receiptData: String? = null,

    @Column(name = "purchased_at", nullable = false)
    val purchasedAt: LocalDateTime,

    @Column(name = "expires_at")
    val expiresAt: LocalDateTime? = null,

    @Column(name = "price_amount")
    val priceAmount: Long? = null,

    @Column(name = "purchase_token")
    val purchaseToken: String? = null

)
