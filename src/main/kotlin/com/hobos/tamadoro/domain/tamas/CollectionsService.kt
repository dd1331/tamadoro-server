package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CollectionsService(
    private val userInventoryRepository: UserInventoryRepository,
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val characterRepository: TamaCatalogRepository,
    private val userTamaRepository: UserTamaRepository,
    private val settingsRepository: UserCollectionSettingsRepository,
    private val tamaCatalogRepository: TamaCatalogRepository,
) {
    // Query responsibilities have been moved to application layer

    @Transactional
    fun setActiveBackground(userId: UUID, url: String): Map<String, Any?> {
        // must own or be free
        val bg = backgroundRepository.findByUrl(url).orElseGet { backgroundRepository.save(BackgroundEntity(
            url = url,
            theme = "TODO()",
            title = "TODO()",
            userId = userId,
        )) }
        if (bg.isPremium) {
            throw IllegalStateException("User does not own background")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId), backgroundEntity = bg)) }
        settings.backgroundEntity = bg // <--- Th
        settingsRepository.save(settings)
        return mapOf("activeBackgroundId" to bg.id)
    }

    @Transactional
    fun setActiveMusic(userId: UUID, id: Long): Map<String, Any?> {
        val track = musicTrackRepository.findById(id).orElseThrow { NoSuchElementException("Music not found") }
        if (track.isPremium && !userTamaRepository.existsByUser_IdAndId(userId,  id)) {
            throw IllegalStateException("User does not own music track")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId))) }
        settings.activeMusicId = id
        settingsRepository.save(settings)
        return mapOf("activeMusicId" to id)
    }

    @Transactional
    fun setActiveCharacter(userId: UUID, id: Long): Map<String, Any?> {
        val tama = characterRepository.findById(id).orElseThrow { NoSuchElementException("Tama not found") }
        if (tama.isPremium && !userTamaRepository.existsByUser_IdAndId(userId,  id)) {
            throw IllegalStateException("User does not own tama")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId))) }
        settings.activeTamaId = id
        settingsRepository.save(settings)
        return mapOf("activeTamaId" to id)
    }



    @Transactional
    fun purchaseCharacter(userId: UUID, id: Long): Map<String, Any?> {
        val tama = tamaCatalogRepository.findById(id).orElseThrow { NoSuchElementException("Tama not found") }
        if (userTamaRepository.existsByUser_IdAndId(userId, id)) {
            return mapOf("purchasedTamaId" to id)
        }
        userTamaRepository.save(
            UserTama(
                user = requireUser(userId),
                tama = tama,
                id = TODO(),
                name = TODO(),
                isActive = TODO()
            )
        )
        return mapOf("purchasedTamaId" to id)
    }

    private fun requireUser(userId: UUID): User {
        // lazy load via inventory repo to avoid adding UserRepository dependency here
        val inv = userInventoryRepository.findByUserId(userId)
        return inv.map { it.user }.orElseThrow { NoSuchElementException("User not found") }
    }


}


