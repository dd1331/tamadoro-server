package com.hobos.tamadoro.domain.collections

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CollectionsService(
    private val userInventoryRepository: UserInventoryRepository
) {
    // For now serve static catalog; persistence for ownership would be added later
    private val backgrounds = listOf(
        BackgroundItem("bg1", "Sunrise", "gradient:orange", "gradient", focusColor = "#FFA500", breakColor = "#FFD580", theme = "light", isPremium = false),
        BackgroundItem("bg2", "Forest", "image:forest.jpg", "image", theme = "nature", imagePath = "/images/forest.jpg", isPremium = true)
    )

    private val music = listOf(
        MusicItem("m1", "Rain", "rain", "nature", volume = 70, isPremium = false),
        MusicItem("m2", "Focus Tones", "focus_tones", "focus", volume = 60, isPremium = true)
    )

    private val characters = listOf(
        CharacterItem("c1", "Tomato", "tomato", 1, isPremium = false),
        CharacterItem("c2", "Dragon", "dragon", 2, isPremium = true)
    )

    fun getBackgrounds(userId: UUID): List<BackgroundItem> = backgrounds
    fun getMusic(userId: UUID): List<MusicItem> = music
    fun getCharacters(userId: UUID): List<CharacterItem> = characters

    fun setActiveBackground(userId: UUID, id: String): Map<String, Any?> {
        // Placeholder: return active id; a real impl would persist selection per user
        return mapOf("activeBackgroundId" to id)
    }

    fun setActiveMusic(userId: UUID, id: String): Map<String, Any?> {
        return mapOf("activeMusicId" to id)
    }

    fun setActiveCharacter(userId: UUID, id: String): Map<String, Any?> {
        return mapOf("activeCharacterId" to id)
    }

    fun purchaseBackground(userId: UUID, id: String): Map<String, Any?> {
        // Placeholder: validate coins in inventory; skip actual deduction for now
        return mapOf("purchasedBackgroundId" to id)
    }

    fun purchaseMusic(userId: UUID, id: String): Map<String, Any?> {
        return mapOf("purchasedMusicId" to id)
    }

    fun purchaseCharacter(userId: UUID, id: String): Map<String, Any?> {
        return mapOf("purchasedCharacterId" to id)
    }
}


