package com.hobos.tamadoro.domain.tamagotchi

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository interface for Tamagotchi entity.
 */
@Repository
interface TamagotchiRepository : JpaRepository<Tamagotchi, UUID> {
    /**
     * Find all tamagotchis for a user.
     */
    fun findByUserId(userId: UUID): List<Tamagotchi>
    
    /**
     * Find a user's active tamagotchi.
     */
    fun findByUserIdAndIsActiveTrue(userId: UUID): Tamagotchi?
    
    /**
     * Find all tamagotchis for a user with a specific type.
     */
    fun findByUserIdAndType(userId: UUID, type: TamagotchiType): List<Tamagotchi>
    
    /**
     * Find all tamagotchis for a user with a specific rarity.
     */
    fun findByUserIdAndRarity(userId: UUID, rarity: TamagotchiRarity): List<Tamagotchi>
    
    /**
     * Find all tamagotchis for a user with a specific growth stage.
     */
    fun findByUserIdAndGrowthStage(userId: UUID, growthStage: TamagotchiGrowthStage): List<Tamagotchi>
    
    /**
     * Count the number of tamagotchis a user has.
     */
    fun countByUserId(userId: UUID): Long
    
    /**
     * Count the number of tamagotchis a user has by rarity.
     */
    fun countByUserIdAndRarity(userId: UUID, rarity: TamagotchiRarity): Long
    
    /**
     * Find the highest level tamagotchi for a user.
     */
    @Query("SELECT t FROM Tamagotchi t WHERE t.user.id = :userId ORDER BY t.level DESC, t.experience DESC")
    fun findHighestLevelTamagotchiByUserId(userId: UUID): Tamagotchi?
    
    /**
     * Find unhealthy tamagotchis for a user (low happiness, high hunger, or low energy).
     */
    @Query("SELECT t FROM Tamagotchi t WHERE t.user.id = :userId AND (t.happiness <= 30 OR t.hunger >= 80 OR t.energy <= 20)")
    fun findUnhealthyTamagotchisByUserId(userId: UUID): List<Tamagotchi>
}