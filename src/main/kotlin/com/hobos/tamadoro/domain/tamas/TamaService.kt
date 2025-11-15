package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.domain.tamas.entity.TamaCatalogEntity
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Domain service for tama-related business logic.
 */
@Service
class TamaService(
    private val userTamaRepository: UserTamaRepository,
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
    ): UserTama {
        val tama = UserTama(
            user = user,
            name = name,
            tama = tamaCatalogEntity,
        )

        val savedTama = userTamaRepository.save(tama)

        // If this is the user's first tama, make it active
        val userInventory = userInventoryRepository.findByUserId(user.id)


        return savedTama
    }


    /**
     * Adds experience to a tama.
     */
    @Transactional
    fun addExperienceToTama(tamaId: Long, amount: Int): UserTama {
        val tama = userTamaRepository.findById(tamaId)
            .orElseThrow { NoSuchElementException("Tama not found with ID: $tamaId") }

        if (amount <= 0) {
            return tama
        }

        tama.addExperience(amount)
        return userTamaRepository.save(tama)
    }

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
    
