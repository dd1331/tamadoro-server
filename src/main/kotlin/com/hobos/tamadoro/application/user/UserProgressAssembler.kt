package com.hobos.tamadoro.application.user

import com.hobos.tamadoro.domain.collections.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import org.springframework.stereotype.Component
import java.util.UUID
@Component
class UserProgressAssembler(
    private val userTamaRepository: UserTamaRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository
) {
    fun assemble(userId: UUID): UserProgressDto {
        val tamas = userTamaRepository.findByUserId(userId)
        val tamaDtos = tamas.map { tama ->
            TamaProgressDto(
                id = tama.id,
                tamaCatalogId = tama.tama?.id,
                name = tama.name.takeIf { it.isNotBlank() } ?: tama.tama?.title,
                experience = tama.experience,
                happiness = tama.happiness,
                energy = tama.energy,
                hunger = tama.hunger,
                isActive = tama.isActive
            )
        }

        val activeTamaId = resolveActiveTamaId(userId, tamas.firstOrNull { it.isActive }?.id)

        return UserProgressDto(
            tamas = tamaDtos,
            activeTamaId = activeTamaId?.toString(),
            careItems = CareItemsDto(
                food = 0,
                toy = 0,
                snack = 0
            )
        )
    }

    private fun resolveActiveTamaId(userId: UUID, fallback: Long?): Long? {
        val settings = userCollectionSettingsRepository.findByUser_Id(userId)
        if (settings.isPresent) {
            settings.get().activeTamaId?.let { return it }
        }
        return fallback
    }
}
