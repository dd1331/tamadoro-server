package com.hobos.tamadoro.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for Subscription entity.
 */
@Repository
interface SubscriptionRepository : JpaRepository<Subscription, UUID> {
    /**
     * Find all subscriptions of a user ordered by start date desc.
     */
    fun findByUserIdOrderByStartDateDesc(userId: UUID): List<Subscription>

    /**
     * Find current active subscriptions of a user.
     */
    fun findByUserIdAndStatus(userId: UUID, status: SubscriptionStatus): List<Subscription>

    /**
     * Find all active subscriptions.
     */
    fun findByStatus(status: SubscriptionStatus): List<Subscription>
}