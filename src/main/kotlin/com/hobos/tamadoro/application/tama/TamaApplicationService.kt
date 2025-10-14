package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.UserTama
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.tama.TamaGrowthStage
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for tama-related use cases.
 */
@Service
class TamaApplicationService(
    private val tamaService: TamaService,
    private val userTamaRepository: UserTamaRepository,
    private val userRepository: UserRepository,
    private val tamaCatalogRepository: TamaCatalogRepository,
) {
    /**
     * Gets all tamas for a user.
     */
    fun getTamas(userId: UUID): List<TamaDto> {


        val tamas = userTamaRepository.findByUserId(userId)
        return tamas.map { TamaDto.fromEntity(it) }
    }
    
    /**
     * Gets a specific tama by ID.
     */
    fun getTama(userId: UUID, tamaId: UUID): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        return TamaDto.fromEntity(tama)
    }
    
    /**
     * Creates a new tama.
     */
    @Transactional
    fun createTama(userId: UUID, request: CreateTamaRequest): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val catalog = tamaCatalogRepository.findById(request.id)
        .orElseThrow { NoSuchElementException("Tama not found with ID: $request.id") }
        val name = request.name?.ifBlank { null } ?: catalog.title

        val tama = tamaService.createTama(
            user = user,
            name = name,
            catalog

        )
        
        return TamaDto.fromEntity(tama)
    }
    
    /**
     * Updates a tama.
     */
    @Transactional
    fun updateTama(userId: UUID, tamaId: UUID, request: UpdateTamaRequest): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        val stage = request.stage?.let {
            try {
                TamaGrowthStage.valueOf(it.uppercase())
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid tama stage: $it")
            }
        }

        val updatedTama = tamaService.updateTama(
            tama = tama,
            name = request.name,
            stage = stage
        )

        return TamaDto.fromEntity(updatedTama)
    }
    
    /**
     * Deletes a tama.
     */
    @Transactional
    fun deleteTama(userId: UUID, tamaId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        userTamaRepository.delete(tama)
    }
    
    /**
     * Feeds a tama.
     */
    @Transactional
    fun feedTama(userId: UUID, tamaId: UUID): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        val fedTama = tamaService.feedTama(tamaId)
        return TamaDto.fromEntity(fedTama)
    }
    
    /**
     * Plays with a tama.
     */
    @Transactional
    fun playWithTama(userId: UUID, tamaId: UUID): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        val playedTama = tamaService.playWithTama(tamaId)
        return TamaDto.fromEntity(playedTama)
    }
    
    /**
     * Activates a tama.
     */
    @Transactional
    fun activateTama(userId: UUID, tamaId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        tamaService.setActiveTama(userId, tamaId)
    }

    @Transactional
    fun addExperience(userId: UUID, tamaId: UUID, amount: Int): TamaDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }

        val updated = tamaService.addExperienceToTama(tamaId, amount)
        return TamaDto.fromEntity(updated)
    }
}

/**
 * DTO for tama data.
 */
data class TamaDto(
    val id: Long,
    val userId: UUID,
    val url: String,
    val name: String,
) {
    companion object {
        fun fromEntity(entity: UserTama): TamaDto {
            return TamaDto(

                userId = entity.user.id,
                url = entity.tama.url,
                name = entity.name,
                id = entity.id,
            )
        }
    }
}

/**
 * Request for creating a tama.
 */
data class CreateTamaRequest(
    val id: Long,
    val name: String? = null
)

/**
 * Request for updating a tama.
 */
data class UpdateTamaRequest(
    val name: String? = null,
    val stage: String? = null
) 
