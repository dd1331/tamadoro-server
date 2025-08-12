package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "tamagotchi_catalog_stages", indexes = [Index(name = "idx_tamagotchi_id", columnList = "tamagotchi_id")])
data class TamagotchiCatalogStageEntity(
    @Id
    @Column(name = "id", nullable = false, length = 128)
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tamagotchi_id", nullable = false)
    val tamagotchi: TamagotchiCatalogEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    val name: StageName,

    @Column(name = "experience", nullable = false)
    val experience: Int,

    @Column(name = "max_experience", nullable = false)
    val maxExperience: Int,

    @Column(name = "level", nullable = false)
    val level: Int,

    @Column(name = "url", nullable = false)
    val url: String,
)


