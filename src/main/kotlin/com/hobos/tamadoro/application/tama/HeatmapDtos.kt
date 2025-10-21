package com.hobos.tamadoro.application.tama

enum class HeatmapNodeKind { REGION, GROUP }

enum class HeatmapEntryKind { GROUP, INDIVIDUAL }

data class HeatmapRankEntryDto(
    val id: Long,
    val name: String,
    val experience: Long,
    val averageExperience: Double? = null,
    val memberCount: Long? = null,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val region: String? = null,
    val regionName: String? = null,
    val groupId: Long? = null,
    val extra: Map<String, Any?>? = null,
    val kind: HeatmapEntryKind
)

data class HeatmapNodeDto(
    val key: String,
    val label: String,
    val kind: HeatmapNodeKind,
    val totalExperience: Long,
    val unitCount: Long,
    val averageExperience: Double? = null,
    val topEntry: HeatmapRankEntryDto? = null,
    val entries: List<HeatmapRankEntryDto>,
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val extra: Map<String, Any?>? = null
)
