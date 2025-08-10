package com.hobos.tamadoro.domain.tamagotchi

import com.hobos.tamadoro.domain.inventory.UserInventory
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

/**
 * Domain service for tamagotchi-related business logic.
 */
@Service
class TamagotchiService(
    private val tamagotchiRepository: TamagotchiRepository,
    private val userInventoryRepository: UserInventoryRepository
) {
    /**
     * Creates a new tamagotchi for a user.
     */
    @Transactional
    fun createTamagotchi(
        user: User,
        name: String,
        type: TamagotchiType,
        rarity: TamagotchiRarity
    ): Tamagotchi {
        val tamagotchi = Tamagotchi(
            user = user,
            name = name,
            type = type,
            rarity = rarity
        )
        
        val savedTamagotchi = tamagotchiRepository.save(tamagotchi)
        
        // If this is the user's first tamagotchi, make it active
        val userInventory = userInventoryRepository.findByUserId(user.id)
        if (userInventory.isPresent && userInventory.get().activeTamagotchi == null) {
            userInventory.get().changeActiveTamagotchi(savedTamagotchi)
            userInventoryRepository.save(userInventory.get())
        }
        
        return savedTamagotchi
    }
    
    /**
     * Gets a random tamagotchi based on rarity probabilities.
     */
    fun getRandomTamagotchi(user: User, name: String): Tamagotchi {
        // Determine rarity based on probabilities
        val rarity = determineRandomRarity()
        
        // Determine type based on available types
        val type = TamagotchiType.values().random()
        
        return createTamagotchi(user, name, type, rarity)
    }
    
    /**
     * Feeds a tamagotchi to reduce hunger.
     */
    @Transactional
    fun feedTamagotchi(tamagotchiId: UUID, amount: Int = 20): Tamagotchi {
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        tamagotchi.feed(amount)
        return tamagotchiRepository.save(tamagotchi)
    }
    
    /**
     * Plays with a tamagotchi to increase happiness.
     */
    @Transactional
    fun playWithTamagotchi(tamagotchiId: UUID, amount: Int = 15): Tamagotchi {
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        tamagotchi.play(amount)
        return tamagotchiRepository.save(tamagotchi)
    }
    
    /**
     * Lets a tamagotchi rest to restore energy.
     */
    @Transactional
    fun restTamagotchi(tamagotchiId: UUID, amount: Int = 30): Tamagotchi {
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        tamagotchi.rest(amount)
        return tamagotchiRepository.save(tamagotchi)
    }
    
    /**
     * Adds experience to a tamagotchi.
     */
    @Transactional
    fun addExperienceToTamagotchi(tamagotchiId: UUID, amount: Int): Tamagotchi {
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        tamagotchi.addExperience(amount)
        return tamagotchiRepository.save(tamagotchi)
    }
    
    /**
     * Sets a tamagotchi as the active tamagotchi for a user.
     */
    @Transactional
    fun setActiveTamagotchi(userId: UUID, tamagotchiId: UUID): UserInventory {
        val userInventory = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("User inventory not found for user ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        userInventory.changeActiveTamagotchi(tamagotchi)
        return userInventoryRepository.save(userInventory)
    }
    
    /**
     * Updates the status of all tamagotchis for a user.
     */
    @Transactional
    fun updateTamagotchiStatus(userId: UUID) {
        val tamagotchis = tamagotchiRepository.findByUserId(userId)
        
        tamagotchis.forEach { tamagotchi ->
            tamagotchi.updateStatus()
            tamagotchiRepository.save(tamagotchi)
        }
    }
    
    /**
     * Gets all tamagotchis for a user.
     */
    fun getAllTamagotchisForUser(userId: UUID): List<Tamagotchi> {
        return tamagotchiRepository.findByUserId(userId)
    }
    
    /**
     * Gets a user's active tamagotchi.
     */
    fun getActiveTamagotchiForUser(userId: UUID): Tamagotchi? {
        return tamagotchiRepository.findByUserIdAndIsActiveTrue(userId)
    }
    
    /**
     * Gets all unhealthy tamagotchis for a user.
     */
    fun getUnhealthyTamagotchisForUser(userId: UUID): List<Tamagotchi> {
        return tamagotchiRepository.findUnhealthyTamagotchisByUserId(userId)
    }
    
    /**
     * Updates a tamagotchi.
     */
    @Transactional
    fun updateTamagotchi(tamagotchi: Tamagotchi, name: String?): Tamagotchi {
        name?.let { tamagotchi.name = it }
        return tamagotchiRepository.save(tamagotchi)
    }
    
    /**
     * Rewards a tamagotchi for completed pomodoro sessions.
     */
    @Transactional
    fun rewardTamagotchiForPomodoro(userId: UUID, completedMinutes: Int) {
        val activeTamagotchi = getActiveTamagotchiForUser(userId) ?: return
        
        // Add experience based on completed minutes (1 exp per minute)
        val experienceToAdd = completedMinutes
        activeTamagotchi.addExperience(experienceToAdd)
        
        // Improve happiness slightly
        activeTamagotchi.happiness = (activeTamagotchi.happiness + 5).coerceAtMost(100)
        
        // Increase hunger slightly (focusing makes you hungry!)
        activeTamagotchi.hunger = (activeTamagotchi.hunger + 2).coerceAtMost(100)
        
        tamagotchiRepository.save(activeTamagotchi)
    }
    
    /**
     * Determines a random rarity based on probabilities.
     */
    private fun determineRandomRarity(): TamagotchiRarity {
        val random = Random.nextInt(100)
        
        return when {
            random < 50 -> TamagotchiRarity.COMMON      // 50%
            random < 80 -> TamagotchiRarity.RARE        // 30%
            random < 95 -> TamagotchiRarity.EPIC        // 15%
            random < 99 -> TamagotchiRarity.LEGENDARY   // 4%
            else -> TamagotchiRarity.MYTHIC             // 1%
        }
    }
}