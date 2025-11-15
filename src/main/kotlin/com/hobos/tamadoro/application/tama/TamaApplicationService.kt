package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import com.hobos.tamadoro.domain.tamas.TamaService
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
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
            ownedTamas.associateBy { it.catalog.id }

        // 액티브는 하나만 (여러 개면 첫 것만 사용, 가능하면 도메인에서 강제)
        val activeCatalogId: Long? =
            ownedTamas.firstOrNull { it.isActive }?.catalog?.id

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



    @Transactional
    fun createCustomTama(userId: UUID,  request: CustomTamaRequest): TamaDto {
        val tama = tamaCatalogRepository.save(
            TamaCatalog(
            url = request.url,
            theme = "TODO()",
            title = "TODO()"
        )
        )
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


        val tama = tamaService.createTama(
            userId = userId,
            name = request.name,
            catalogId = request.id

        )
        
        return TamaDto.fromEntity(tama)
    }
    
    /**
     * Updates a tama.
     */
    @Transactional
    fun updateTama(userId: UUID, tamaId: Long, request: UpdateTamaRequest): TamaDto {

        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        


        val updatedTama = tamaService.updateTama(
            tama = tama,
            name = request.name,
        )

        return TamaDto.fromEntity(updatedTama)
    }
    
    /**
     * Deletes a tama.
     */
    @Transactional
    fun deleteTama(userId: UUID, tamaId: Long) {
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        userTamaRepository.delete(tama)
    }
    


    /**
     * Activates a tama.
     */
    @Transactional
    fun activateTama(userId: UUID, tamaId: Long) {

        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }
        println("@@@@"+tama.user.id+ tama.id +"@"+ tamaId+"@"+ userId)
        // Ensure the tama belongs to the user
        if (tama.user.id != userId) {
            throw IllegalArgumentException("Tama does not belong to the user")
        }
        
        tamaService.setActiveTama(userId, tamaId)
    }

    fun assignGroup(userId: UUID, tamaId: Long, groupId: Long) {


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
    val isActive: Boolean = false,
    val isOwned: Boolean = false,
    val backgroundUrl: String? = null,
) {
    companion object {
        fun fromEntity(entity: UserTama): TamaDto {
            val displayName = entity.name


            return TamaDto(
                tamaCatalogId = entity.catalog.id,
                url = entity.catalog.url,
                id = entity.id,
                name = displayName,
                experience = entity.experience,
                isActive = entity.isActive,
            )
        }

        fun fromCatalog(
            catalog: TamaCatalog,
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
    val name: String
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
