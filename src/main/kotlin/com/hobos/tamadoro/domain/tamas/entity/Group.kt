package com.hobos.tamadoro.domain.tamas.entity

import com.hobos.tamadoro.domain.common.Country
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tama_groups")
class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "avatar")
    var avatar: String? = null

    @Column(name = "background")
    var background: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false)
    var country: Country = Country.KR
}
