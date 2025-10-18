package com.hobos.tamadoro.application.collections

import com.hobos.tamadoro.application.collections.model.BackgroundItem
import com.hobos.tamadoro.application.collections.model.MusicItem
import com.hobos.tamadoro.application.collections.model.Stage
import com.hobos.tamadoro.application.collections.model.TamaItem
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.CollectionsService
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.TamaCatalogStageRepository
import com.hobos.tamadoro.domain.collections.UserCollectionSettings
import com.hobos.tamadoro.domain.collections.UserCollectionSettingsRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CollectionsApplicationService(
    private val collectionsService: CollectionsService,
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val characterRepository: TamaCatalogRepository,
    private val characterStageRepository: TamaCatalogStageRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
) {
    data class UserCollectionSettingsDto(
//        val activeBackgroundId: Long?,
        val activeBackgroundUrl: String?,
        val activeMusicId: Long?,
        val activeTamaId: Long?,
    )

    fun getSettings(userId: UUID): UserCollectionSettingsDto {
        val settings = userCollectionSettingsRepository.findByUser_Id(userId).orElse(null)
        val bgUrl = settings?.backgroundEntity?.url
        return UserCollectionSettingsDto(
//            activeBackgroundId = settings?.activeBackgroundId,
            activeBackgroundUrl = bgUrl,
            activeMusicId = settings?.activeMusicId,
            activeTamaId = settings?.activeTamaId,
        )
    }

    fun getBackgrounds(): List<BackgroundItem> =
        backgroundRepository.findAll().takeIf { it.isNotEmpty() }?.map {
            BackgroundItem(id = it.id, title = it.title, theme = it.theme, url = it.url, isPremium = it.isPremium)
        } ?: listOf(
            BackgroundItem(1L, "Sunrise",  url = "https://picsum.photos/600/600", theme = "light", isPremium = false),
            BackgroundItem(2L, "Forest", theme = "nature", url = "https://picsum.photos/800/600", isPremium = true),
            BackgroundItem(3L, "Sunrise2",  url = "https://picsum.photos/200", theme = "light", isPremium = false),
            BackgroundItem(4L, "Forest2", theme = "nature", url = "https://picsum.photos/400", isPremium = true)
        )
    fun setActiveBackground(userId: UUID, url: String) = collectionsService.setActiveBackground(userId, url)

    fun getSound(): List<MusicItem> =
        musicTrackRepository.findAll().takeIf { it.isNotEmpty() }?.map {
            MusicItem(id = it.id, title = it.title, theme = it.theme, url = it.url, isPremium = it.isPremium, resource = it.resource)
        } ?: listOf(
            MusicItem(1L, "Rain", "rain", "https://picsum.photos/600/600",  isPremium = false, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
            MusicItem(2L, "Focus Tones2", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3"),
            MusicItem(3L, "Focus Tones3", "focus_tones", "https://picsum.photos/800/600", isPremium = false, resource="https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"),
            MusicItem(4L, "Focus Tones4", "focus_tones", "https://picsum.photos/800/600", isPremium = true, resource="https://github.com/rafaelreis-hotmart/Audio-Sample-files/raw/master/sample.mp3")
        )

	fun setActiveMusic(userId: UUID, id: Long) = collectionsService.setActiveMusic(userId, id)

    fun getTamas(): List<TamaItem> {
        val entities = characterRepository.findAll()
        if (entities.isEmpty()) return listOf(
            TamaItem(
                id = 1L,
                title = "Pippo",
                theme = "cat",
                isPremium = false,
                stages = listOf(
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 10, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                ),
            ),
            TamaItem(
                id = 2L,
                title = "Drogo",
                theme = "dragon",
                isPremium = true,
                stages = listOf(
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 20, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image.png"),
                ),
            ),
            TamaItem(
                id = 3L,
                title = "Drog4",
                theme = "dragon4",
                isPremium = true,
                stages = listOf(
                    Stage(name = com.hobos.tamadoro.domain.collections.StageName.EGG, experience = 0, maxExperience = 20, level = 1, url = "https://blurb-bucket.s3.ap-northeast-2.amazonaws.com/image2.png"),
                ),
            ),
        )
        val stagesByCharacter = characterStageRepository.findAll().groupBy { it.id }
        return entities.map { ch ->
            TamaItem(
                id = ch.id,
                title = ch.title,
                theme = ch.theme,
                isPremium = ch.isPremium,
                stages = stagesByCharacter[ch.id].orEmpty().sortedBy { it.level }.map { st ->
                    Stage(name = st.name, experience = st.experience, maxExperience = st.maxExperience, level = st.level, url = st.url)
                },
            )
        }
    }
    fun setActiveTama(userId: UUID, id: Long) = collectionsService.setActiveCharacter(userId, id)
    fun purchaseTama(userId: UUID, id: Long) = collectionsService.purchaseCharacter(userId, id)
}


