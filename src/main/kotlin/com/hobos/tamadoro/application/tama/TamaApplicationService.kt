package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tamas.TamaCatalogEntity
import com.hobos.tamadoro.domain.tamas.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.UserTama
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
        val catalogs = tamaCatalogRepository.findAll()
        val ownedTamas = userTamaRepository.findByUserId(userId)

        // 카탈로그별 1:1 소유라는 가정 → 단일 매핑
        val ownedByCatalogId: Map<Long, UserTama> =
            ownedTamas.associateBy { it.tama.id }

        // 액티브는 하나만 (여러 개면 첫 것만 사용, 가능하면 도메인에서 강제)
        val activeCatalogId: Long? =
            ownedTamas.firstOrNull { it.isActive }?.tama?.id

        // (선택) 데이터 방어: 중복 감지
        // if (ownedTamas.size != ownedByCatalogId.size) {
        //     log.warn("User $userId has duplicate ownerships per catalog. Check unique constraint.")
        // }

        return catalogs.map { catalog ->
            val owned: UserTama? = ownedByCatalogId[catalog.id]
            val isOwned = owned != null
            val isActive = (catalog.id == activeCatalogId)

            val displayName = owned?.let { ut ->
                ut.name?.takeIf { it.isNotBlank() } ?: catalog.title
            } ?: catalog.title

            TamaDto.fromCatalog(
                catalog = catalog,
                isOwned = isOwned,
                isActive = isActive,
                name = displayName,
                id = owned?.id
            )
        }
    }
    /**
     * Gets a specific tama by ID.
     */
    fun getTama(userId: UUID, tamaId: Long): TamaDto {
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

    @Transactional
    fun createCustomTama(userId: UUID,  request: CustomTamaRequest): TamaDto {
        val tama = tamaCatalogRepository.save(TamaCatalogEntity(
            url = request.url,
            theme = "TODO()",
            title = "TODO()"
        ))
        println("@@@@"+ tama.toString())
        return ownTama(userId, OwnTamaRequest(
            id = tama.id,
            name = request.name
        ))
    }
    
    /**
     * Creates a new tama.
     */
    @Transactional
    fun ownTama(userId: UUID, request: OwnTamaRequest): TamaDto {
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
    fun updateTama(userId: UUID, tamaId: Long, request: UpdateTamaRequest): TamaDto {
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
    fun deleteTama(userId: UUID, tamaId: Long) {
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
    fun feedTama(userId: UUID, tamaId: Long): TamaDto {
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
    fun playWithTama(userId: UUID, tamaId: Long): TamaDto {
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
    fun activateTama(userId: UUID, tamaId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        println("@@@@"+tama.user.id+ tama.id +"@"+ tamaId+"@"+ userId)
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        tamaService.setActiveTama(userId, tamaId)
    }

    @Transactional
    fun addExperience(userId: UUID, tamaId: Long, amount: Int): TamaDto {
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
    val id: Long? = null,
    val tamaCatalogId: Long?,
    val url: String,
    val name: String? = null,
    val experience: Int? = null,
    val happiness: Int? = null,
    val energy: Int? = null,
    val hunger: Int? = null,
    val isActive: Boolean = false,
    val isOwned: Boolean = false,
    val backgroundUrl: String? = null,
) {
    companion object {
        fun fromEntity(entity: UserTama): TamaDto {
            val displayName = entity.name
                .takeIf { it.isNotBlank() }
                ?: entity.tama.title

            return TamaDto(
                tamaCatalogId = entity.tama.id,
                url = entity.tama.url,
                id = entity.id,
                name = displayName,
                experience = entity.experience,
                happiness = entity.happiness,
                energy = entity.energy,
                hunger = entity.hunger,
                isActive = entity.isActive,
            )
        }

        fun fromCatalog(
            catalog: TamaCatalogEntity,
            isOwned: Boolean = false,
            isActive: Boolean = false,
            name: String? = null,
            id: Long? = null,
        ): TamaDto {
            return TamaDto(
                id = id,                           // 카탈로그-only는 null
                tamaCatalogId = catalog.id,
                url = catalog.url,
                name = name ?: catalog.title,
                isActive = isActive,
                isOwned = isOwned
            )
        }
    }
}

/**
 * Request for creating a tama.
 */
data class OwnTamaRequest(
    val id: Long,
    val name: String? = null
)

data class CustomTamaRequest(
    val url: String,
    val name: String,
)

/**
 * Request for updating a tama.
 */
data class UpdateTamaRequest(
    val name: String? = null,
    val stage: String? = null
) 
