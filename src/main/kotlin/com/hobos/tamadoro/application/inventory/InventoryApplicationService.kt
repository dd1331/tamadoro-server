package com.hobos.tamadoro.application.inventory

import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.domain.inventory.UserInventory
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.tama.TamaRepository
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
    private val tamaRepository: TamaRepository,
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

        return toDto(userId, inventory)
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

        when {
            amount > 0 -> inventory.addCoins(amount)
            amount < 0 -> {
                val success = inventory.removeCoins(-amount)
                if (!success) throw IllegalArgumentException("Insufficient coins")
            }
        }

        val savedInventory = userInventoryRepository.save(inventory)
        return toDto(userId, savedInventory)
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

        when {
            amount > 0 -> inventory.addGems(amount)
            amount < 0 -> {
                val success = inventory.removeGems(-amount)
                if (!success) throw IllegalArgumentException("Insufficient gems")
            }
        }

        val savedInventory = userInventoryRepository.save(inventory)
        return toDto(userId, savedInventory)
    }
    
    /**
     * Sets the active tama.
     */
    @Transactional
    fun setActiveTama(userId: UUID, tamaId: UUID): InventoryDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseGet { createDefaultInventory(user) }

        val tama = tamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }

        inventory.changeActiveTama(tama)
        val savedInventory = userInventoryRepository.save(inventory)
        return toDto(userId, savedInventory)
    }
    
    /**
     * Creates a default inventory for a user.
     */
    private fun createDefaultInventory(user: User): UserInventory {
        val inventory = UserInventory(user = user)
        return userInventoryRepository.save(inventory)
    }

    private fun toDto(userId: UUID, inventory: UserInventory): InventoryDto {
        val tamas = tamaRepository.findByUserId(userId).map { TamaDto.fromEntity(it) }
        return InventoryDto.from(inventory, tamas)
    }
}

/**
 * DTO for inventory data.
 */
data class InventoryDto(
    val coins: Int,
    val gems: Int,
    val tamas: List<TamaDto>,
    val activeTamaId: Long?
) {
    companion object {
        fun from(entity: UserInventory, tamas: List<TamaDto>): InventoryDto = InventoryDto(
            coins = entity.coins,
            gems = entity.gems,
            tamas = tamas,
            activeTamaId = entity.activeTama?.id
        )
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
 * Request for setting active tama.
 */
data class SetActiveTamaRequest(
    val id: UUID
) 
