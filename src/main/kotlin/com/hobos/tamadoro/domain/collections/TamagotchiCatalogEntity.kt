package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tamagotchi_catalog")
data class TamagotchiCatalogEntity(
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

    @OneToMany(mappedBy = "tamagotchi", fetch = FetchType.LAZY)
    val stages: List<TamagotchiCatalogStageEntity> = emptyList(),
)


