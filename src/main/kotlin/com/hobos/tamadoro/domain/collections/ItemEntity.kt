package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass


@MappedSuperclass
abstract class ItemEntity(
    @Column(name = "theme", nullable = false) var theme: String,
    @Column(name = "title", nullable = false) var title: String,
    @Column(name = "url", nullable = false) var url: String,
    @Column(name = "is_premium", nullable = false) val isPremium: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var  id: Long = 0

}
