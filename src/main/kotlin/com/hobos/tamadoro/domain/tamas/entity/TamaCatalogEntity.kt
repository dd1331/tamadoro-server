package com.hobos.tamadoro.domain.tamas.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "tama_catalog")
class TamaCatalogEntity(


    theme: String,

    title: String,

    url: String,

    isPremium: Boolean = false,

    @Column(name = "is_custom")
    val isCustom: Boolean = false,


): ItemEntity(theme, title, url, isPremium)


