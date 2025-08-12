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
    private val characterRepository: TamagotchiCatalogRepository,
    private val characterStageRepository: TamagotchiCatalogStageRepository,
    private val ownershipRepository: UserCollectionOwnershipRepository,
    private val settingsRepository: UserCollectionSettingsRepository,
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
        TamagotchiItem(
            id = "char_001",
            title = "Pippo",
            theme = "cat",
            isPremium = false,
            stages = listOf(
                Stage(
                    name = StageName.EGG,
                    experience = 0,
                    maxExperience = 10,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"
                ),
                Stage(
                    name = StageName.BABY,
                    experience = 0,
                    maxExperience = 50,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"
                ),
                Stage(
                    name = StageName.CHILD,
                    experience = 0,
                    maxExperience = 150,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"
                ),
                Stage(
                    name = StageName.TEEN,
                    experience = 0,
                    maxExperience = 300,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"
                ),
                Stage(
                    name = StageName.ADULT,
                    experience = 0,
                    maxExperience = 500,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"
                )
            ),
            happiness = 80,
            hunger = 20,
            energy = 90
        ),
        TamagotchiItem(
            id = "char_002",
            title = "Drogo",
            theme = "dragon",
            isPremium = true,
            stages = listOf(
                Stage(
                    name = StageName.EGG,
                    experience = 0,
                    maxExperience = 20,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"
                ),
                Stage(
                    name = StageName.BABY,
                    experience = 0,
                    maxExperience = 80,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"
                ),
                Stage(
                    name = StageName.CHILD,
                    experience = 0,
                    maxExperience = 200,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"
                ),
                Stage(
                    name = StageName.TEEN,
                    experience = 0,
                    maxExperience = 450,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"
                ),
                Stage(
                    name = StageName.ADULT,
                    experience = 0,
                    maxExperience = 700,
                    level = 1,
                    url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"
                )
            ),
            happiness = 95,
            hunger = 10,
            energy = 85
        ),
    )

    private fun BackgroundEntity.toModel(): BackgroundItem =
        BackgroundItem(id = id, title = title, theme = theme, url = url, isPremium = isPremium)

    private fun MusicTrackEntity.toModel(): MusicItem =
        MusicItem(id = id, title = title, theme = theme, url = url, isPremium = isPremium, resource = resource)

    private fun TamagotchiCatalogEntity.toModel(stages: List<TamagotchiCatalogStageEntity>): TamagotchiItem =
        TamagotchiItem(
            id = id,
            title = title,
            theme = theme,
            isPremium = isPremium,
            stages = stages.sortedBy { it.level }.map { stage ->
                Stage(
                    name = stage.name,
                    experience = stage.experience,
                    maxExperience = stage.maxExperience,
                    level = stage.level,
                    url = stage.url,
                )
            },
            happiness = happiness,
            hunger = hunger,
            energy = energy,
        )

    fun getBackgrounds(): List<BackgroundItem> =
        (backgroundRepository.findAll().takeIf { it.isNotEmpty() }?.map { it.toModel() }) ?: backgrounds

    fun getSound(): List<MusicItem> =
        (musicTrackRepository.findAll().takeIf { it.isNotEmpty() }?.map { it.toModel() }) ?: sound

    fun getCharacters(): List<TamagotchiItem> {
        val entities = characterRepository.findAll()
        if (entities.isEmpty()) return characters
        val stagesByCharacter = characterStageRepository.findAll().groupBy { it.tamagotchi.id }
        return entities.map { ch -> ch.toModel(stagesByCharacter[ch.id].orEmpty()) }
    }

    @Transactional
    fun setActiveBackground(userId: UUID, id: String): Map<String, Any?> {
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
    fun setActiveMusic(userId: UUID, id: String): Map<String, Any?> {
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
    fun setActiveCharacter(userId: UUID, id: String): Map<String, Any?> {
        val tama = characterRepository.findById(id).orElseThrow { NoSuchElementException("Tamagotchi not found") }
        if (tama.isPremium && !ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.TAMAGOTCHI, id)) {
            throw IllegalStateException("User does not own tamagotchi")
        }
        val settings = settingsRepository.findByUser_Id(userId)
            .orElseGet { settingsRepository.save(UserCollectionSettings(user = requireUser(userId))) }
        settings.activeTamagotchiId = id
        settingsRepository.save(settings)
        return mapOf("activeTamagotchiId" to id)
    }

    @Transactional
    fun purchaseBackground(userId: UUID, id: String): Map<String, Any?> {
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
    fun purchaseMusic(userId: UUID, id: String): Map<String, Any?> {
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
    fun purchaseCharacter(userId: UUID, id: String): Map<String, Any?> {
        val tama = characterRepository.findById(id).orElseThrow { NoSuchElementException("Tamagotchi not found") }
        if (ownershipRepository.existsByUser_IdAndCategoryAndItemId(userId, CollectionCategory.TAMAGOTCHI, id)) {
            return mapOf("purchasedTamagotchiId" to id)
        }
        if (tama.isPremium) deductCoins(userId, amount = 300)
        ownershipRepository.save(
            UserCollectionOwnership(
                user = requireUser(userId),
                category = CollectionCategory.TAMAGOTCHI,
                itemId = id,
            )
        )
        return mapOf("purchasedTamagotchiId" to id)
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


