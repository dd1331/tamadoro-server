package com.hobos.tamadoro.application.collections

import com.hobos.tamadoro.application.collections.model.TamaItem
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.CollectionsService
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CollectionsApplicationService(
    private val collectionsService: CollectionsService,
    private val backgroundRepository: BackgroundRepository,
    private val characterRepository: TamaCatalogRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
) {
    data class UserCollectionSettingsDto(
        val activeBackgroundUrl: String?,
        val activeTamaId: Long?,
    )

    fun getSettings(userId: UUID): UserCollectionSettingsDto {
        val settings = userCollectionSettingsRepository.findByUser_Id(userId).orElse(null)
        val bgUrl = settings?.activeBackground?.url
        return UserCollectionSettingsDto(
//            activeBackgroundId = settings?.activeBackgroundId,
            activeBackgroundUrl = bgUrl,
            activeTamaId = settings?.activeTama?.id,
        )
    }


    fun setActiveBackground(userId: UUID, url: String) = collectionsService.setActiveBackground(userId, url)



    fun getTamas(): List<TamaItem> {
        val entities = characterRepository.findAll()
        if (entities.isEmpty()) return listOf(
            TamaItem(
                id = 1L,
                title = "Pippo",
                theme = "cat",
                isPremium = false,
            ),
            TamaItem(
                id = 2L,
                title = "Drogo",
                theme = "dragon",
                isPremium = true,
            ),
            TamaItem(
                id = 3L,
                title = "Drog4",
                theme = "dragon4",
                isPremium = true,
            ),
        )
        return entities.map { ch ->
            TamaItem(
                id = ch.id,
                title = ch.title,
                theme = ch.theme,
                isPremium = ch.isPremium,
            )
        }
    }
    fun setActiveTama(userId: UUID, id: Long) = collectionsService.setActiveCharacter(userId, id)
    fun purchaseTama(userId: UUID, id: Long) = collectionsService.purchaseCharacter(userId, id)
}


