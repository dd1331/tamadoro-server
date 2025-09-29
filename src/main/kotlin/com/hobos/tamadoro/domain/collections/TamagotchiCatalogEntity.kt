package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tama_catalog")
class TamaCatalogEntity(


    @OneToMany(mappedBy = "tama", fetch = FetchType.LAZY)
    val stages: List<TamaCatalogStageEntity> = emptyList(),
    theme: String,

    title: String,

    url:String


): ItemEntity(theme, title, url)


