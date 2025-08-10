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

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column(name = "name")
    var name: String,

    @Column(name = "is_premium")
    var isPremium: Boolean = false,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var subscription: Subscription? = null
    /**
     * Updates the user's profile information
     */
    fun updateProfile(name: String?, email: String?) {
        name?.let { this.name = it }
        email?.let { this.email = it }
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Records a login event
     */
    fun recordLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }

    /**
     * Activates premium subscription
     */
    fun activatePremium(type: SubscriptionType, endDate: LocalDateTime) {
        this.isPremium = true
        if (this.subscription == null) {
            this.subscription = Subscription(
                user = this,
                type = type,
                startDate = LocalDateTime.now(),
                endDate = endDate,
                status = SubscriptionStatus.ACTIVE
            )
        } else {
            this.subscription?.let {
                it.type = type
                it.startDate = LocalDateTime.now()
                it.endDate = endDate
                it.status = SubscriptionStatus.ACTIVE
            }
        }
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Cancels premium subscription
     */
    fun cancelPremium() {
        this.subscription?.let {
            it.status = SubscriptionStatus.CANCELLED
        }
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * Checks if subscription is expired and updates status accordingly
     */
    fun checkSubscriptionStatus() {
        this.subscription?.let {
            if (it.endDate.isBefore(LocalDateTime.now()) && it.status == SubscriptionStatus.ACTIVE) {
                it.status = SubscriptionStatus.EXPIRED
                this.isPremium = false
                this.updatedAt = LocalDateTime.now()
            }
        }
    }

    /**
     * Checks if the user has an active premium subscription
     */
    fun hasPremium(): Boolean {
        return isPremium && subscription?.isActive() == true
    }
}
