package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "tama_catalog_stages")
 class TamaCatalogStageEntity(


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
){
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "id")
   var  id: Long = 0

}


