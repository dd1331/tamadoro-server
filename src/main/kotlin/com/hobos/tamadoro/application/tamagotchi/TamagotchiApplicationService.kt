package com.hobos.tamadoro.application.tamagotchi

import com.hobos.tamadoro.domain.tamagotchi.Tamagotchi
import com.hobos.tamadoro.domain.tamagotchi.TamagotchiRepository
import com.hobos.tamadoro.domain.tamagotchi.TamagotchiService
import com.hobos.tamadoro.domain.tamagotchi.TamagotchiType
import com.hobos.tamadoro.domain.tamagotchi.TamagotchiRarity
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for tamagotchi-related use cases.
 */
@Service
class TamagotchiApplicationService(
    private val tamagotchiService: TamagotchiService,
    private val tamagotchiRepository: TamagotchiRepository,
    private val userRepository: UserRepository
) {
    /**
     * Gets all tamagotchis for a user.
     */
    fun getTamagotchis(userId: UUID): List<TamagotchiDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchis = tamagotchiRepository.findByUserId(userId)
        return tamagotchis.map { TamagotchiDto.fromEntity(it) }
    }
    
    /**
     * Gets a specific tamagotchi by ID.
     */
    fun getTamagotchi(userId: UUID, tamagotchiId: UUID): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        return TamagotchiDto.fromEntity(tamagotchi)
    }
    
    /**
     * Creates a new tamagotchi.
     */
    @Transactional
    fun createTamagotchi(userId: UUID, request: CreateTamagotchiRequest): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiService.createTamagotchi(
            user = user,
            name = request.name,
            type = TamagotchiType.valueOf(request.type.uppercase()),
            rarity = TamagotchiRarity.valueOf(request.rarity.uppercase())
        )
        
        return TamagotchiDto.fromEntity(tamagotchi)
    }
    
    /**
     * Updates a tamagotchi.
     */
    @Transactional
    fun updateTamagotchi(userId: UUID, tamagotchiId: UUID, request: UpdateTamagotchiRequest): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        val updatedTamagotchi = tamagotchiService.updateTamagotchi(
            tamagotchi = tamagotchi,
            name = request.name
        )
        
        return TamagotchiDto.fromEntity(updatedTamagotchi)
    }
    
    /**
     * Deletes a tamagotchi.
     */
    @Transactional
    fun deleteTamagotchi(userId: UUID, tamagotchiId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        tamagotchiRepository.delete(tamagotchi)
    }
    
    /**
     * Feeds a tamagotchi.
     */
    @Transactional
    fun feedTamagotchi(userId: UUID, tamagotchiId: UUID): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        val fedTamagotchi = tamagotchiService.feedTamagotchi(tamagotchiId)
        return TamagotchiDto.fromEntity(fedTamagotchi)
    }
    
    /**
     * Plays with a tamagotchi.
     */
    @Transactional
    fun playWithTamagotchi(userId: UUID, tamagotchiId: UUID): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        val playedTamagotchi = tamagotchiService.playWithTamagotchi(tamagotchiId)
        return TamagotchiDto.fromEntity(playedTamagotchi)
    }
    
    /**
     * Activates a tamagotchi.
     */
    @Transactional
    fun activateTamagotchi(userId: UUID, tamagotchiId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }
        
        // Ensure the tamagotchi belongs to the user
        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }
        
        tamagotchiService.setActiveTamagotchi(userId, tamagotchiId)
    }

    @Transactional
    fun addExperience(userId: UUID, tamagotchiId: UUID, amount: Int): TamagotchiDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val tamagotchi = tamagotchiRepository.findById(tamagotchiId)
            .orElseThrow { NoSuchElementException("Tamagotchi not found with ID: $tamagotchiId") }

        if (tamagotchi.user.id != userId) {
            throw IllegalArgumentException("Tamagotchi does not belong to the user")
        }

        val updated = tamagotchiService.addExperienceToTamagotchi(tamagotchiId, amount)
        return TamagotchiDto.fromEntity(updated)
    }
}

/**
 * DTO for tamagotchi data.
 */
data class TamagotchiDto(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val type: String,
    val rarity: String,
    val level: Int,
    val experience: Int,
    val maxExperience: Int,
    val isActive: Boolean,
    val acquiredAt: String,
    val growthStage: String,
    val happiness: Int,
    val hunger: Int,
    val energy: Int,
    val lastInteraction: String,
    val wellBeingScore: Int
) {
    companion object {
        fun fromEntity(entity: Tamagotchi): TamagotchiDto {
            return TamagotchiDto(
                id = entity.id,
                userId = entity.user.id,
                name = entity.name,
                type = entity.type.name,
                rarity = entity.rarity.name,
                level = entity.level,
                experience = entity.experience,
                maxExperience = entity.maxExperience,
                isActive = entity.isActive,
                acquiredAt = entity.acquiredAt.toString(),
                growthStage = entity.growthStage.name,
                happiness = entity.happiness,
                hunger = entity.hunger,
                energy = entity.energy,
                lastInteraction = entity.lastInteraction.toString(),
                wellBeingScore = entity.calculateWellBeingScore()
            )
        }
    }
}

/**
 * Request for creating a tamagotchi.
 */
data class CreateTamagotchiRequest(
    val name: String,
    val type: String,
    val rarity: String
)

/**
 * Request for updating a tamagotchi.
 */
data class UpdateTamagotchiRequest(
    val name: String? = null
) 