package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Domain service for tama-related business logic.
 */
@Service
class TamaService(
    private val userTamaRepository: UserTamaRepository,
    private val userRepository: UserRepository,
    private val tamaCatalogRepository: TamaCatalogRepository,
) {
    /**
     * Creates a new tama for a user.
     */
    @Transactional
    fun createTama(
        userId: UUID,
        name: String,
        catalogId: Long
    ): UserTama {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val catalog = tamaCatalogRepository.findById(catalogId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $catalogId") }

        val tama = UserTama(
            user = user,
            name = name,
            catalog = catalog,
        )

        val savedTama = userTamaRepository.save(tama)



        return savedTama
    }


    /**
     * Adds experience to a tama.
     */


    /**
     * Sets a tama as the active tama for a user.
     */
    @Transactional
    fun setActiveTama(userId: UUID, tamaId: Long): List<UserTama?> {

        val tamas = userTamaRepository.findByUserId(userId)

        val updated = tamas.map { tama ->
            tama.isActive = false
            if (tamaId == tama.id) tama.isActive = true
            tama
        }


        return userTamaRepository.saveAll(updated)
    }


    /**
     * Gets all tamas for a user.
     */
    fun getAllTamasForUser(userId: UUID): List<UserTama> {
        return userTamaRepository.findByUserId(userId)
    }


    /**
     * Updates a tama.
     */
    @Transactional
    fun updateTama(tama: UserTama, name: String?): UserTama {
        name?.let { tama.name = it }
        // TODO: use ownership??
//        stage?.let { tama.growthStage = it }
        return userTamaRepository.save(tama)
    }
}
    
