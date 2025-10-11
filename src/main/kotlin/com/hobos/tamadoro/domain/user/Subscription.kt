package com.hobos.tamadoro.domain.user

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Entity representing a user's subscription.
 */
@Entity
@Table(name = "subscriptions")
class Subscription(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SubscriptionType,
    
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,
    
    @Column(name = "end_date")
    var endDate: LocalDateTime?,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SubscriptionStatus
) {
    /**
     * Checks if the subscription is active
     */
    fun isActive(): Boolean {
        if (status != SubscriptionStatus.ACTIVE) return false
        // UNLIMITED has null endDate; treat as active while status is ACTIVE
        if (endDate == null) return true
        val today: LocalDate = LocalDate.now()
        return !endDate!!.toLocalDate().isBefore(today)
    }

    fun renew(
        type: SubscriptionType,
    ) {
        val today: LocalDate = LocalDate.now()
        // TODO: check if eligible to trial if the type is trial
        // If already subscribed, extend the current active subscription
        val baseDate = if (this.isActive()) this.endDate!!.toLocalDate() else today

        val newEndDate = when (type) {
            SubscriptionType.WEEKLY -> baseDate.plusWeeks(1)
            SubscriptionType.MONTHLY -> baseDate.plusMonths(1)
            SubscriptionType.YEARLY -> baseDate.plusYears(1)
            SubscriptionType.TRIAL -> baseDate.plusWeeks(1)
            SubscriptionType.UNLIMITED -> null
        }?.let { LocalDateTime.of(it, LocalTime.MAX) }
        this.endDate = newEndDate
        this.type = type
        this.status = SubscriptionStatus.ACTIVE
    }
    // No extend method: renewals are represented as separate Subscription rows
}