package com.hobos.tamadoro.domain.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * User entity representing a user in the system.
 * This is the aggregate root for the User bounded context.
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "providerId")
    var providerId: String,

    @Column(name = "is_premium")
    var isPremium: Boolean = false,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var subscriptions: MutableList<Subscription> = mutableListOf()
    /**
     * Updates the user's profile information
     */

    /**
     * Records a login event
     */
    fun recordLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }

    /**
     * Activates premium subscription
     */
    fun activatePremium(type: SubscriptionType, endDate: LocalDateTime?) {
        val newSubscription = Subscription(
            user = this,
            type = type,
            startDate = LocalDateTime.now(),
            endDate = endDate,
            status = SubscriptionStatus.ACTIVE
        )
        this.subscriptions.add(newSubscription)
        this.isPremium = true
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Cancels premium subscription
     */
    fun cancelPremium() {
        // Cancel the latest active subscription, if any
        val latestActive = this.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE }
            .maxByOrNull { it.startDate }
        latestActive?.status = SubscriptionStatus.CANCELLED
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Checks if subscription is expired and updates status accordingly
     */
    fun checkSubscriptionStatus() {
        // Day-based: expire if endDate's date is before today (unlimited has null)
        val today = java.time.LocalDate.now()
        var anyActive = false
        this.subscriptions.forEach { sub ->
            if (sub.status == SubscriptionStatus.ACTIVE) {
                val endDate = sub.endDate?.toLocalDate()
                if (endDate != null && endDate.isBefore(today)) {
                    sub.status = SubscriptionStatus.EXPIRED
                } else {
                    anyActive = true
                }
            }
        }
        this.isPremium = anyActive
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Checks if the user has an active premium subscription
     */
    fun hasPremium(): Boolean {
        return subscriptions.any { it.isActive() }
    }
}
