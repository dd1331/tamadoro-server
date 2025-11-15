package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.tamas.repository.GroupRankingProjection
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


data class TamaRankDto(
    val id: Long,
    val name: String?,
    val experience: Int,
    val url: String,
    val backgroundUrl: String?
)

open class TamaGroupRankDto(
    open val groupId: Long,
    open val groupName: String,
    open val totalExperience: Long,
    open val memberCount: Long,
    open val averageExperience: Double?,
    open val avatarUrl: String?,
    open val backgroundUrl: String?
)

data class TamaGroupHeatmapDto(
    override val groupId: Long,
    override val groupName: String,
    override val totalExperience: Long,
    override val memberCount: Long,
    override val averageExperience: Double?,
    val region: String?,
    val regionName: String?,
    override val avatarUrl: String?,
    override val backgroundUrl: String?
) : TamaGroupRankDto(groupId, groupName, totalExperience, memberCount, averageExperience, avatarUrl, backgroundUrl) {}

@Service
class TamaRankApplicationService(
    private val tamaRepository: UserTamaRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
    private val backgroundRepository: BackgroundRepository,
) {
    companion object {
        private const val DEFAULT_HEATMAP_NODE_LIMIT = 50
        private const val DEFAULT_HEATMAP_ENTRY_LIMIT = 10
    }

    private fun toTamaGroupHeatmapDto(projection: GroupRankingProjection): TamaGroupHeatmapDto {
        val memberCount = projection.tamaCount
        val average = if (memberCount > 0) {
            projection.totalExperience.toDouble() / memberCount
        } else {
            null
        }
        return TamaGroupHeatmapDto(
            groupId = projection.groupId,
            groupName = projection.groupName,
            totalExperience = projection.totalExperience,
            memberCount = memberCount,
            averageExperience = average,
            region = projection.country.name,
            regionName = projection.country.label,
            avatarUrl = projection.avatar,
            backgroundUrl = projection.background
        )
    }

    private fun toGroupRankDto(projection: GroupRankingProjection): TamaGroupRankDto {
        val memberCount = projection.tamaCount
        val average = if (memberCount > 0) {
            projection.totalExperience.toDouble() / memberCount
        } else {
            null
        }
        return TamaGroupRankDto(
            groupId = projection.groupId,
            groupName = projection.groupName,
            totalExperience = projection.totalExperience,
            memberCount = memberCount,
            averageExperience = average,
            avatarUrl = projection.avatar,
            backgroundUrl = projection.background
        )
    }

    private fun toGroupHeatmapEntry(projection: GroupRankingProjection): HeatmapRankEntryDto {
        val dto = toTamaGroupHeatmapDto(projection)
        return HeatmapRankEntryDto(
            id = dto.groupId,
            name = dto.groupName,
            experience = dto.totalExperience,
            averageExperience = dto.averageExperience,
            memberCount = dto.memberCount,
            avatarUrl = dto.avatarUrl,
            backgroundUrl = dto.backgroundUrl,
            region = dto.region,
            regionName = dto.regionName,
            kind = HeatmapEntryKind.GROUP
        )
    }

    private fun toIndividualHeatmapEntry(userTama: UserTama, projection: GroupRankingProjection): HeatmapRankEntryDto {
        val name = if (userTama.name != null) userTama.name else userTama.catalog.title
        return HeatmapRankEntryDto(
            id = userTama.id,
            name = name,
            experience = userTama.experience.toLong(),
            avatarUrl = userTama.catalog.url,
            region = projection.country.name,
            regionName = projection.country.label,
            groupId = projection.groupId,
            extra = mapOf("groupName" to projection.groupName),
            kind = HeatmapEntryKind.INDIVIDUAL
        )
    }

    private fun resolveLimit(limit: Int?, defaultValue: Int): Int = limit?.takeIf { it > 0 } ?: defaultValue


    @Transactional(readOnly = true)
    fun getRankWithPaging(request: PagingRequest): PagedResponse<TamaRankDto> {
        val pageable = PageRequest.of(request.page, request.size, Sort.by(Sort.Direction.DESC, "experience"))
        val rankedPage = tamaRepository.findAllWithTamaOrderByExperienceDesc(pageable)

        // Batch load user settings for all ranked users
        val userIds = rankedPage.content.map { it.user.id }.toSet()
        val settingsByUserId = userCollectionSettingsRepository.findByUser_IdIn(userIds).associateBy { it.user.id }

        // Map result in-memory
        val content = rankedPage.content.map { ut ->
            val settings = settingsByUserId[ut.user.id]
            val bgUrl = settings?.activeBackground?.url
            TamaRankDto(
                id = ut.id,
                name = ut.name,
                experience = ut.experience,
                url = ut.catalog.url,
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

    @Transactional(readOnly = true)
    fun getGroupRankingWithPaging(request: PagingRequest): PagedResponse<TamaGroupRankDto> {
        val pageable = PageRequest.of(request.page, request.size)
        val rankedPage = tamaRepository.findGroupRanking(pageable)

        val content = rankedPage.content.map(::toGroupRankDto)

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

    @Transactional(readOnly = true)
    fun getRegionalHeatmap(nodeLimit: Int?, entryLimit: Int?): List<HeatmapNodeDto> {
        val rankedGroups = tamaRepository.findGroupRanking(Pageable.unpaged()).content
        if (rankedGroups.isEmpty()) {
            return emptyList()
        }

        val effectiveNodeLimit = resolveLimit(nodeLimit, DEFAULT_HEATMAP_NODE_LIMIT)
        val effectiveEntryLimit = resolveLimit(entryLimit, DEFAULT_HEATMAP_ENTRY_LIMIT)

        val nodes = rankedGroups.groupBy { it.country }.map { (country, projections) ->
            val sortedProjections = projections.sortedByDescending { it.totalExperience }
            val entries = sortedProjections.map(::toGroupHeatmapEntry).take(effectiveEntryLimit)
            val totalExperience = sortedProjections.sumOf { it.totalExperience }
            val groupCount = sortedProjections.size.toLong()
            val average = if (groupCount > 0) totalExperience.toDouble() / groupCount else null
            val topEntry = entries.firstOrNull()
            HeatmapNodeDto(
                key = country.name,
                label = country.label,
                kind = HeatmapNodeKind.REGION,
                totalExperience = totalExperience,
                unitCount = groupCount,
                averageExperience = average,
                topEntry = topEntry,
                entries = entries,
                avatarUrl = topEntry?.avatarUrl,
                backgroundUrl = topEntry?.backgroundUrl,
                extra = mapOf("region" to country.name)
            )
        }.sortedByDescending { it.totalExperience }

        return nodes.take(effectiveNodeLimit)
    }

    @Transactional(readOnly = true)
    fun getGroupHeatmap(nodeLimit: Int?, entryLimit: Int?): List<HeatmapNodeDto> {
        val rankedGroups = tamaRepository.findGroupRanking(Pageable.unpaged()).content
        if (rankedGroups.isEmpty()) {
            return emptyList()
        }

        val effectiveNodeLimit = resolveLimit(nodeLimit, DEFAULT_HEATMAP_NODE_LIMIT)
        val effectiveEntryLimit = resolveLimit(entryLimit, DEFAULT_HEATMAP_ENTRY_LIMIT)

        val sortedGroups = rankedGroups.sortedByDescending { it.totalExperience }
        val selectedGroups = sortedGroups.take(effectiveNodeLimit)

        return selectedGroups.map { projection ->
            val members = tamaRepository.findTopMembersByGroupId(
                projection.groupId, PageRequest.of(0, effectiveEntryLimit)
            )
            val memberEntries = members.map { toIndividualHeatmapEntry(it, projection) }
            val topEntry = memberEntries.firstOrNull()
            val unitCount = projection.tamaCount
            val totalExperience = projection.totalExperience
            HeatmapNodeDto(
                key = projection.groupId.toString(),
                label = projection.groupName,
                kind = HeatmapNodeKind.GROUP,
                totalExperience = totalExperience,
                unitCount = unitCount,
                averageExperience = if (unitCount > 0) totalExperience.toDouble() / unitCount else null,
                topEntry = topEntry,
                entries = memberEntries,
                avatarUrl = projection.avatar ?: topEntry?.avatarUrl,
                backgroundUrl = projection.background ?: topEntry?.backgroundUrl,
                extra = mapOf(
                    "groupId" to projection.groupId,
                    "region" to projection.country.name,
                    "regionName" to projection.country.label
                )
            )
        }
    }
}
