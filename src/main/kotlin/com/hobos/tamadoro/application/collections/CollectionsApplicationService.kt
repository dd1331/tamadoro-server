package com.hobos.tamadoro.application.collections

import com.hobos.tamadoro.domain.collections.CollectionsService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CollectionsApplicationService(
    private val collectionsService: CollectionsService
) {
    fun getBackgrounds() = collectionsService.getBackgrounds()
    fun setActiveBackground(userId: UUID, id: String) = collectionsService.setActiveBackground(userId, id)
    fun purchaseBackground(userId: UUID, id: String) = collectionsService.purchaseBackground(userId, id)

    fun getSound() = collectionsService.getSound()
    fun setActiveMusic(userId: UUID, id: String) = collectionsService.setActiveMusic(userId, id)
    fun purchaseMusic(userId: UUID, id: String) = collectionsService.purchaseMusic(userId, id)

    fun getCharacters() = collectionsService.getCharacters()
    fun setActiveCharacter(userId: UUID, id: String) = collectionsService.setActiveCharacter(userId, id)
    fun purchaseCharacter(userId: UUID, id: String) = collectionsService.purchaseCharacter(userId, id)
}


