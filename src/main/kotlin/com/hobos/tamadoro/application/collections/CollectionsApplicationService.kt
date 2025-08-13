package com.hobos.tamadoro.application.collections

import com.hobos.tamadoro.application.collections.model.BackgroundItem
import com.hobos.tamadoro.application.collections.model.MusicItem
import com.hobos.tamadoro.application.collections.model.Stage
import com.hobos.tamadoro.application.collections.model.TamagotchiItem
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.CollectionsService
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogRepository
import com.hobos.tamadoro.domain.collections.TamagotchiCatalogStageRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CollectionsApplicationService(
    private val collectionsService: CollectionsService,
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val characterRepository: TamagotchiCatalogRepository,
    private val characterStageRepository: TamagotchiCatalogStageRepository,
) {
    fun getBackgrounds(): List<BackgroundItem> =
        backgroundRepository.findAll().takeIf { it.isNotEmpty() }?.map {
            BackgroundItem(id = it.id, title = it.title, theme = it.theme, url = it.url, isPremium = it.isPremium)
        } ?: listOf(
            BackgroundItem("bg1", "Sunrise",  url = "https://picsum.photos/600/600", theme = "light", isPremium = false),
            BackgroundItem("bg2", "Forest", theme = "nature", url = "https://picsum.photos/800/600", isPremium = true),
            BackgroundItem("bg3", "Sunrise2",  url = "https://picsum.photos/200", theme = "light", isPremium = false),
            BackgroundItem("bg4", "Forest2", theme = "nature", url = "https://picsum.photos/400", isPremium = true)
        )
    fun setActiveBackground(userId: UUID, id: String) = collectionsService.setActiveBackground(userId, id)
    fun purchaseBackground(userId: UUID, id: String) = collectionsService.purchaseBackground(userId, id)

    fun getSound(): List<MusicItem> =
        musicTrackRepository.findAll().takeIf { it.isNotEmpty() }?.map {
            MusicItem(id = it.id, title = it.title, theme = it.theme, url = it.url, isPremium = it.isPremium, resource = it.resource)
        } ?: listOf(
            MusicItem("m1", "Rain", "rain", "https://picsum.photos/600/600",  isPremium = false, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
            MusicItem("m2", "Focus Tones2", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"),
            MusicItem("m3", "Focus Tones3", "focus_tones", "https://picsum.photos/800/600", isPremium = false, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
            MusicItem("m4", "Focus Tones4", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3")
        )

	fun setActiveMusic(userId: UUID, id: String) = collectionsService.setActiveMusic(userId, id)
	fun purchaseMusic(userId: UUID, id: String) = collectionsService.purchaseMusic(userId, id)

    fun getTamagotchis(): List<TamagotchiItem> {
        val entities = characterRepository.findAll()
        if (entities.isEmpty()) return listOf(
            TamagotchiItem(
                id = "char_001",
                title = "Pippo",
                theme = "cat",
                isPremium = false,
                stages = listOf(
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 10, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.BABY, experience = 0, maxExperience = 50, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.CHILD, experience = 0, maxExperience = 150, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.TEEN, experience = 0, maxExperience = 300, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.ADULT, experience = 0, maxExperience = 500, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png")
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
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 20, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.BABY, experience = 0, maxExperience = 80, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.CHILD, experience = 0, maxExperience = 200, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.TEEN, experience = 0, maxExperience = 450, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.ADULT, experience = 0, maxExperience = 700, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png")
                ),
                happiness = 95,
                hunger = 10,
                energy = 85
            ),
            TamagotchiItem(
                id = "char_003",
                title = "Drog4",
                theme = "dragon4",
                isPremium = true,
                stages = listOf(
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 20, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.BABY, experience = 0, maxExperience = 80, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.CHILD, experience = 0, maxExperience = 200, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.TEEN, experience = 0, maxExperience = 450, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.ADULT, experience = 0, maxExperience = 700, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png")
                ),
                happiness = 95,
                hunger = 10,
                energy = 85
            ),
        )
        val stagesByCharacter = characterStageRepository.findAll().groupBy { it.tamagotchi.id }
        return entities.map { ch ->
            TamagotchiItem(
                id = ch.id,
                title = ch.title,
                theme = ch.theme,
                isPremium = ch.isPremium,
                stages = stagesByCharacter[ch.id].orEmpty().sortedBy { it.level }.map { st ->
                    Stage(name = st.name, experience = st.experience, maxExperience = st.maxExperience, level = st.level, url = st.url)
                },
                happiness = ch.happiness,
                hunger = ch.hunger,
                energy = ch.energy,
            )
        }
    }
    fun setActiveTamagotchi(userId: UUID, id: String) = collectionsService.setActiveCharacter(userId, id)
    fun purchaseTamagotchi(userId: UUID, id: String) = collectionsService.purchaseCharacter(userId, id)
}


