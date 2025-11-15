package com.hobos.tamadoro.application.user

import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
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
                tamaCatalogId = tama.catalog?.id,
                name = tama.name.takeIf { it.isNotBlank() } ?: tama.catalog?.title,
                experience = tama.experience,
                isActive = tama.isActive
            )
        }

        val activeTamaId = resolveActiveTamaId(userId, tamas.firstOrNull { it.isActive }?.id)

        return UserProgressDto(
            tamas = tamaDtos,
            activeTamaId = activeTamaId,
        )
    }

    private fun resolveActiveTamaId(userId: UUID, fallback: Long?): Long? {
        val settings = userCollectionSettingsRepository.findByUser_Id(userId)
        if (settings.isPresent) {
            settings.get().activeTama?.let { return it.id }
        }
        return fallback
    }
}
