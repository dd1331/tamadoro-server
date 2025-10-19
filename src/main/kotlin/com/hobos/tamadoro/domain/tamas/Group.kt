package com.hobos.tamadoro.domain.tamas

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "groups")
class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "name")
    val name: String = ""

    @Column()
    val avatar: String = ""

    @Column()
    val background: String = ""

}
