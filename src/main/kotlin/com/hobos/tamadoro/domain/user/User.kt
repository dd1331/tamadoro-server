package com.hobos.tamadoro.domain.user

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
) {

//    companion object {
//        fun create(providerId: String) = User(providerId = providerId)
//    }
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
        this.updatedAt = LocalDateTime.now()
    }


    fun activeSubscription(): Subscription? =
        subscriptions
            .filter { it.isActive() }
            .maxByOrNull { it.startDate }

    /**
     * Starts a subscription and tracks its lifecycle entry.
     */
    fun startSubscription(type: SubscriptionType, startDateTime: LocalDateTime = LocalDateTime.now()): Subscription {
        val startDate = startDateTime.toLocalDate()
        val endDate: LocalDateTime? = when (type) {
            SubscriptionType.WEEKLY -> LocalDateTime.of(startDate.plusWeeks(1), LocalTime.MAX)
            SubscriptionType.MONTHLY -> LocalDateTime.of(startDate.plusMonths(1), LocalTime.MAX)
            SubscriptionType.YEARLY -> LocalDateTime.of(startDate.plusYears(1), LocalTime.MAX)
            SubscriptionType.UNLIMITED -> null
            SubscriptionType.TRIAL -> LocalDateTime.of(startDate.plusWeeks(1), LocalTime.MAX)
        }
        val newSubscription = Subscription(
            user = this,
            type = type,
            startDate = startDateTime,
            endDate = endDate,
            status = SubscriptionStatus.ACTIVE
        )
        this.subscriptions.add(newSubscription)
        this.updatedAt = LocalDateTime.now()
        return newSubscription
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
        this.subscriptions.forEach { sub ->
            if (sub.status == SubscriptionStatus.ACTIVE) {
                val endDate = sub.endDate?.toLocalDate()
                if (endDate != null && endDate.isBefore(today)) {
                    sub.status = SubscriptionStatus.EXPIRED
                }
            }
        }
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Checks if the user has an active premium subscription
     */
    fun hasPremium(): Boolean {
        return subscriptions.any { it.isActive() }
    }
}
