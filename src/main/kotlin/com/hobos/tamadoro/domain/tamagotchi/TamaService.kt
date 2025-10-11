package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.inventory.UserInventory
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID
import kotlin.random.Random

/**
 * Domain service for tama-related business logic.
 */
@Service
class TamaService(
    private val tamaRepository: TamaRepository,
    private val userInventoryRepository: UserInventoryRepository
) {
    /**
     * Creates a new tama for a user.
     */
    @Transactional
    fun createTama(
        user: User,
        name: String,
        tamaCatalogEntity: TamaCatalogEntity
    ): Tama {
        val tama = Tama(
            user = user,
            name = name,
            tamaCatalogEntity = tamaCatalogEntity
        )
        
        val savedTama = tamaRepository.save(tama)
        
        // If this is the user's first tama, make it active
        val userInventory = userInventoryRepository.findByUserId(user.id)

        
        return savedTama
    }
    

    
    /**
     * Feeds a tama to reduce hunger.
     */
    @Transactional
    fun feedTama(tamaId: UUID, amount: Int = 20): Tama {
        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        // TODO: use ownership??
//        tama.feed(amount)
        return tamaRepository.save(tama)
    }
    
    /**
     * Plays with a tama to increase happiness.
     */
    @Transactional
    fun playWithTama(tamaId: UUID, amount: Int = 15): Tama {
        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        // TODO: use ownership??
//        tama.play(amount)
        return tamaRepository.save(tama)
    }
    
    /**
     * Lets a tama rest to restore energy.
     */
    @Transactional
    fun restTama(tamaId: UUID, amount: Int = 30): Tama {
        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        // TODO: use ownership??
//        tama.rest(amount)
        return tamaRepository.save(tama)
    }
    
    /**
     * Adds experience to a tama.
     */
    @Transactional
    fun addExperienceToTama(tamaId: UUID, amount: Int): Tama {
        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        // TODO: use ownership??
//        tama.addExperience(amount)
        return tamaRepository.save(tama)
    }
    
    /**
     * Sets a tama as the active tama for a user.
     */
    @Transactional
    fun setActiveTama(userId: UUID, tamaId: UUID): UserInventory {
        val userInventory = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("User inventory not found for user ID: $userId") }
        
        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        userInventory.changeActiveTama(tama)
        return userInventoryRepository.save(userInventory)
    }
    
    /**
     * Updates the status of all tamas for a user.
     */
    @Transactional
    fun updateTamaStatus(userId: UUID) {
        val tamas = tamaRepository.findByUserId(userId)
        
        tamas.forEach { tama ->
            // TODO: use ownership??
//            tama.updateStatus()
            tamaRepository.save(tama)
        }
    }
    
    /**
     * Gets all tamas for a user.
     */
    fun getAllTamasForUser(userId: UUID): List<Tama> {
        return tamaRepository.findByUserId(userId)
    }
    
    /**
     * Gets a user's active tama.
     */
    fun getActiveTamaForUser(userId: UUID): Optional<Tama?> {
        return tamaRepository.findById(userId)
    }
    
    /**
     * Gets all unhealthy tamas for a user.
     */
    fun getUnhealthyTamasForUser(userId: UUID): List<Tama> {
        return tamaRepository.findUnhealthyTamasByUserId(userId)
    }
    
    /**
     * Updates a tama.
     */
    @Transactional
    fun updateTama(tama: Tama, name: String?, stage: TamaGrowthStage?): Tama {
        name?.let { tama.name = it }
        // TODO: use ownership??
//        stage?.let { tama.growthStage = it }
        return tamaRepository.save(tama)
    }
    
    /**
     * Rewards a tama for completed pomodoro sessions.
     */
    @Transactional
    fun rewardTamaForPomodoro(userId: UUID, completedMinutes: Int) {
        // TODO: use ownership??
        val activeTama = getActiveTamaForUser(userId)

        if (!activeTama.isPresent) return
        
        // Add experience based on completed minutes (1 exp per minute)
        val experienceToAdd = completedMinutes
        // TODO: use ownership??
//        activeTama.addExperience(experienceToAdd)
        
        // Improve happiness slightly
        // TODO: use ownership??
//        activeTama.happiness = (activeTama.happiness + 5).coerceAtMost(100)
        
        // Increase hunger slightly (focusing makes you hungry!)
        // TODO: use ownership??
//        activeTama.hunger = (activeTama.hunger + 2).coerceAtMost(100)
        
//        tamaRepository.save(activeTama)
    }
    
    /**
     * Determines a random rarity based on probabilities.
     */
    private fun determineRandomRarity(): TamaRarity {
        val random = Random.nextInt(100)
        
        return when {
            random < 50 -> TamaRarity.COMMON      // 50%
            random < 80 -> TamaRarity.RARE        // 30%
            random < 95 -> TamaRarity.EPIC        // 15%
            random < 99 -> TamaRarity.LEGENDARY   // 4%
            else -> TamaRarity.MYTHIC             // 1%
        }
    }
}
