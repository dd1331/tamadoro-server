package com.hobos.tamadoro.domain.collections

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CollectionsService(
    private val userInventoryRepository: UserInventoryRepository,
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val characterRepository: TamaCatalogRepository,
    private val characterStageRepository: TamaCatalogStageRepository,
    private val ownershipRepository: UserCollectionOwnershipRepository,
    private val settingsRepository: UserCollectionSettingsRepository,
) {
    // Query responsibilities have been moved to application layer

    @Transactional
    fun setActiveBackground(userId: UUID, id: Long): Map<String, Any?> {
        // must own or be free
        val bg = backgroundRepository.findById(id).orElseThrow { NoSuchElementException("Background not found") }
        if (bg.isPremium && !ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.BACKGROUND, id)) {
            throw IllegalStateException("User does not own background")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId))) }
        settings.activeBackgroundId = id
        settingsRepository.save(settings)
        return mapOf("activeBackgroundId" to id)
    }

    @Transactional
    fun setActiveMusic(userId: UUID, id: Long): Map<String, Any?> {
        val track = musicTrackRepository.findById(id).orElseThrow { NoSuchElementException("Music not found") }
        if (track.isPremium && !ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.MUSIC, id)) {
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
        if (tama.isPremium && !ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.TAMA, id)) {
            throw IllegalStateException("User does not own tama")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId))) }
        settings.activeTamaId = id
        settingsRepository.save(settings)
        return mapOf("activeTamaId" to id)
    }

    @Transactional
    fun purchaseBackground(userId: UUID, id: Long): Map<String, Any?> {
        val bg = backgroundRepository.findById(id).orElseThrow { NoSuchElementException("Background not found") }
        if (ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.BACKGROUND, id)) {
            return mapOf("purchasedBackgroundId" to id)
        }
        if (bg.isPremium) deductCoins(userId, amount = 100)
        ownershipRepository.save(
            UserCollectionOwnership(
                user = requireUser(userId),
                category = CollectionCategory.BACKGROUND,
                itemId = id,
            )
        )
        return mapOf("purchasedBackgroundId" to id)
    }

    @Transactional
    fun purchaseMusic(userId: UUID, id: Long): Map<String, Any?> {
        val track = musicTrackRepository.findById(id).orElseThrow { NoSuchElementException("Music not found") }
        if (ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.MUSIC, id)) {
            return mapOf("purchasedMusicId" to id)
        }
        if (track.isPremium) deductCoins(userId, amount = 100)
        ownershipRepository.save(
            UserCollectionOwnership(
                user = requireUser(userId),
                category = CollectionCategory.MUSIC,
                itemId = id,
            )
        )
        return mapOf("purchasedMusicId" to id)
    }

    @Transactional
    fun purchaseCharacter(userId: UUID, id: Long): Map<String, Any?> {
        val tama = characterRepository.findById(id).orElseThrow { NoSuchElementException("Tama not found") }
        if (ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.TAMA, id)) {
            return mapOf("purchasedTamaId" to id)
        }
        if (tama.isPremium) deductCoins(userId, amount = 300)
        ownershipRepository.save(
            UserCollectionOwnership(
                user = requireUser(userId),
                category = CollectionCategory.TAMA,
                itemId = id,
            )
        )
        return mapOf("purchasedTamaId" to id)
    }

    private fun requireUser(userId: UUID): com.hobos.tamadoro.domain.user.User {
        // lazy load via inventory repo to avoid adding UserRepository dependency here
        val inv = userInventoryRepository.findByUserId(userId)
        return inv.map { it.user }.orElseThrow { NoSuchElementException("User not found") }
    }

    private fun deductCoins(userId: UUID, amount: Int) {
        val inv = userInventoryRepository.findByUserId(userId).orElseThrow { NoSuchElementException("Inventory not found") }
        if (!inv.removeCoins(amount)) throw IllegalStateException("Not enough coins")
        userInventoryRepository.save(inv)
    }
}


