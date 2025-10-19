package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tamas.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import org.springframework.data.domain.PageRequest
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
    val url: String,
    val backgroundUrl: String?
)

@Service
class TamaRankApplicationService(
    private val tamaRepository: UserTamaRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
    private val backgroundRepository: BackgroundRepository,
) {


    @Transactional(readOnly = true)
    fun getRankWithPaging(request: PagingRequest): PagedResponse<TamaRankDto> {
        val pageable = PageRequest.of(request.page, request.size, Sort.by(Sort.Direction.DESC, "experience"))
        val rankedPage = tamaRepository.findAllWithTamaOrderByExperienceDesc(pageable)

        // Batch load user settings for all ranked users
        val userIds = rankedPage.content.map { it.user.id }.toSet()
        val settingsByUserId = userCollectionSettingsRepository
            .findByUser_IdIn(userIds)
            .associateBy { it.user.id }

        // Collect unique background IDs and batch fetch backgrounds
        val bgIds = settingsByUserId.values.map { it.backgroundEntity?.id }.toSet()
        val bgUrlById = backgroundRepository.findAllById(bgIds).associate { it.id to it.url }

        // Map result in-memory
        val content = rankedPage.content.map { ut ->
            val settings = settingsByUserId[ut.user.id]
            val bgUrl = settings?.backgroundEntity?.url
            TamaRankDto(
                id = ut.id,
                name = ut.name,
                experience = ut.experience,
                happiness = ut.happiness,
                energy = ut.energy,
                hunger = ut.hunger,
                url = ut.tama.url,
                isActive = ut.isActive,
                backgroundUrl = bgUrl
            )
        }

        return PagedResponse(
            content = content,
            page = rankedPage.number,
            size = rankedPage.size,
            totalElements = rankedPage.totalElements,
            totalPages = rankedPage.totalPages,
            hasNext = rankedPage.hasNext(),
            hasPrevious = rankedPage.hasPrevious()
        )
    }

    fun getGroupRankingWithPaging(request: PagingRequest) {
        val pageable = PageRequest.of(request.page, request.size, Sort.by(Sort.Direction.DESC, "experience"))



    }
}