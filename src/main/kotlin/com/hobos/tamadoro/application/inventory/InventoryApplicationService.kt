package com.hobos.tamadoro.application.inventory

import com.hobos.tamadoro.domain.inventory.UserInventory
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.tamagotchi.TamagotchiRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for inventory-related use cases.
 */
@Service
class InventoryApplicationService(
    private val userInventoryRepository: UserInventoryRepository,
    private val tamagotchiRepository: TamagotchiRepository,
    private val userRepository: UserRepository
) {
    /**
     * Gets a user's inventory.
     */
    fun getInventory(userId: UUID): InventoryDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseGet { createDefaultInventory(user) }
        
        return InventoryDto.fromEntity(inventory)
    }
    
    /**
     * Updates a user's coins.
     */
    @Transactional
    fun updateCoins(userId: UUID, amount: Int): InventoryDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseGet { createDefaultInventory(user) }
        
        if (amount > 0) {
            inventory.addCoins(amount)
        } else {
            val success = inventory.removeCoins(-amount)
            if (!success) {
                throw IllegalArgumentException("Insufficient coins")
            }
        }
        
        val savedInventory = userInventoryRepository.save(inventory)
        return InventoryDto.fromEntity(savedInventory)
    }
    
    /**
     * Updates a user's gems.
     */
    @Transactional
    fun updateGems(userId: UUID, amount: Int): InventoryDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseGet { createDefaultInventory(user) }
        
        if (amount > 0) {
            inventory.addGems(amount)
        } else {
            val success = inventory.removeGems(-amount)
            if (!success) {
                throw IllegalArgumentException("Insufficient gems")
            }
        }
        
        val savedInventory = userInventoryRepository.save(inventory)
        return InventoryDto.fromEntity(savedInventory)
    }
    
    /**
     * Sets the active tamagotchi.
     */
    @Transactional
    fun setActiveTamagotchi(userId: UUID, tamagotchiId: UUID?): InventoryDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseGet { createDefaultInventory(user) }
        
        val tamagotchi = tamagotchiId?.let {
            tamagotchiRepository.findById(it)
                .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $it") }
        }
        
        // Ensure the tamagotchi belongs to the user
        tamagotchi?.let {
            if (it.user.id != userId) {
                throw IllegalArgumentException("Tamagotchi does not belong to the user")
            }
        }
        
        inventory.changeActiveTamagotchi(tamagotchi)
        val savedInventory = userInventoryRepository.save(inventory)
        return InventoryDto.fromEntity(savedInventory)
    }
    
    /**
     * Creates a default inventory for a user.
     */
    private fun createDefaultInventory(user: User): UserInventory {
        val inventory = UserInventory(user = user)
        return userInventoryRepository.save(inventory)
    }
}

/**
 * DTO for inventory data.
 */
data class InventoryDto(
    val userId: UUID,
    val coins: Int,
    val gems: Int,
    val activeTamagotchiId: UUID?,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(entity: UserInventory): InventoryDto {
            return InventoryDto(
                userId = entity.user.id,
                coins = entity.coins,
                gems = entity.gems,
                activeTamagotchiId = entity.activeTamagotchi?.id,
                updatedAt = entity.updatedAt.toString()
            )
        }
    }
}

/**
 * Request for updating coins.
 */
data class UpdateCoinsRequest(
    val amount: Int
)

/**
 * Request for updating gems.
 */
data class UpdateGemsRequest(
    val amount: Int
)

/**
 * Request for setting active tamagotchi.
 */
data class SetActiveTamagotchiRequest(
    val tamagotchiId: UUID?
) 