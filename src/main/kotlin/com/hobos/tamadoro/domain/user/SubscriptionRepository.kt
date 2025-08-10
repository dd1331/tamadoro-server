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
     * Find a subscription by user ID.
     */
    fun findByUserId(userId: UUID): Optional<Subscription>
    
    /**
     * Find all active subscriptions.
     */
    fun findByStatus(status: SubscriptionStatus): List<Subscription>
    
    /**
     * Delete a subscription by user ID.
     */
    fun deleteByUserId(userId: UUID)
}