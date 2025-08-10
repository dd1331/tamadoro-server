package com.hobos.tamadoro.domain.user

import jakarta.persistence.*
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
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: SubscriptionType,
    
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,
    
    @Column(name = "end_date", nullable = false)
    var endDate: LocalDateTime,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SubscriptionStatus
) {
    /**
     * Checks if the subscription is active
     */
    fun isActive(): Boolean {
        return status == SubscriptionStatus.ACTIVE && endDate.isAfter(LocalDateTime.now())
    }
    
    /**
     * Extends the subscription by the specified number of months
     */
    fun extend(months: Int) {
        endDate = endDate.plusMonths(months.toLong())
        if (status == SubscriptionStatus.EXPIRED) {
            status = SubscriptionStatus.ACTIVE
        }
    }
}