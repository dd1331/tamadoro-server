package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tama_catalog")
class TamaCatalogEntity(


    theme: String,

    title: String,

    url: String,

    isPremium: Boolean = false,


): ItemEntity(theme, title, url, isPremium)


