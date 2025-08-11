package com.hobos.tamadoro.domain.user

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
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
    
    // No extend method: renewals are represented as separate Subscription rows
}