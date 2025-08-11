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
        BackgroundItem("bg1", "Sunrise",  url = "https://picsum.photos/600/600", theme = "light", isPremium = false),
        BackgroundItem("bg2", "Forest", theme = "nature", url = "https://picsum.photos/800/600", isPremium = true),
                BackgroundItem("bg3", "Sunrise2",  url = "https://picsum.photos/200", theme = "light", isPremium = false),
    BackgroundItem("bg4", "Forest2", theme = "nature", url = "https://picsum.photos/400", isPremium = true)
    )

    private val sound = listOf(
        MusicItem("m1", "Rain", "rain", "https://picsum.photos/600/600",  isPremium = false, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
        MusicItem("m2", "Focus Tones2", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"),
        MusicItem("m3", "Focus Tones3", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
        MusicItem("m4", "Focus Tones4", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"),
    )

    private val characters = listOf(
        CharacterItem("c1", "Tomato", "https://picsum.photos/600/600", "rest", isPremium = false),
        CharacterItem("c2", "Dragon", "https://picsum.photos/800/600", "tes", isPremium = true)
    )

    fun getBackgrounds(): List<BackgroundItem> = backgrounds
    fun getSound(): List<MusicItem> = sound
    fun getCharacters(): List<CharacterItem> = characters

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


