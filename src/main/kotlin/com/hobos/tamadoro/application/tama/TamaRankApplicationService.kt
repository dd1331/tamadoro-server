package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tama.UserTamaRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


data class TamaRankDto(
    val id: Long,
    val name: String?,
    val experience: Int,
    val happiness: Int,
    val energy: Int,
    val hunger: Int,
    val isActive: Boolean,
    val url: String
)

@Service
class TamaRankApplicationService(
    private val tamaRepository: UserTamaRepository
) {

    @Transactional(readOnly = true)
    fun getRank(): List<TamaRankDto> {
        val sort: Sort = Sort.by(Sort.Direction.DESC, "experience")

        return tamaRepository.findAllWithTamaOrderByCreatedAtDesc()
            .map { ut ->
                println("🐛 UserTama: id=${ut.id}, name=${ut.name}, url=${ut.tama.url}")
                TamaRankDto(
                    id = ut.id,
                    name = ut.name,
                    experience = ut.experience,
                    happiness = ut.happiness,
                    energy = ut.energy,
                    hunger = ut.hunger,
                    url = ut.tama.url,
                    isActive = ut.isActive
                )
            }
    }
}