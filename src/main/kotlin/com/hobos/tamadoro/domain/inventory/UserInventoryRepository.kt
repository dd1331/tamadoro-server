package com.hobos.tamadoro.domain.inventory

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for UserInventory entity.
 */
@Repository
interface UserInventoryRepository : JpaRepository<UserInventory, UUID> {
    /**
     * Find a user's inventory by user ID.
     */
    fun findByUserId(userId: UUID): Optional<UserInventory>
    
    /**
     * Check if a user has an inventory.
     */
    fun existsByUserId(userId: UUID): Boolean
    
    /**
     * Find all user inventories with a specific active tama.
     */
    fun findByActiveTamaId(tamaId: UUID): Optional<UserInventory>
}