package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tama_catalog")
data class TamaCatalogEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    val id: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "theme", nullable = false)
    val theme: String,

    @Column(name = "is_premium", nullable = false)
    val isPremium: Boolean,

    @Column(name = "happiness", nullable = false)
    val happiness: Int,

    @Column(name = "hunger", nullable = false)
    val hunger: Int,

    @Column(name = "energy", nullable = false)
    val energy: Int,

    @OneToMany(mappedBy = "tama", fetch = FetchType.LAZY)
    val stages: List<TamaCatalogStageEntity> = emptyList(),
)


