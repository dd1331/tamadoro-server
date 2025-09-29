package com.hobos.tamadoro.domain.tama

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for Tama entity.
 */
@Repository
interface TamaRepository : JpaRepository<Tama, UUID> {
    /**
     * Find all tamas for a user.
     */
    fun findByUserId(userId: UUID): List<Tama>
    
    /**
     * Find all tamas for a user with a specific type.
     */
    fun findByUserIdAndType(userId: UUID, type: TamaType): List<Tama>
    
    /**
     * Find all tamas for a user with a specific rarity.
     */
    fun findByUserIdAndRarity(userId: UUID, rarity: TamaRarity): List<Tama>
    

    
    /**
     * Count the number of tamas a user has.
     */
    fun countByUserId(userId: UUID): Long
    
    /**
     * Count the number of tamas a user has by rarity.
     */
    fun countByUserIdAndRarity(userId: UUID, rarity: TamaRarity): Long
    
    /**
     * Find the highest level tama for a user.
     */
    @Query("SELECT t FROM Tama t WHERE t.user.id = :userId")
    fun findHighestLevelTamaByUserId(userId: UUID): Tama?
    
    /**
     * Find unhealthy tamas for a user (low happiness, high hunger, or low energy).
     */
    @Query("SELECT t FROM Tama t WHERE t.user.id = :userId")
    fun findUnhealthyTamasByUserId(userId: UUID): List<Tama>
}