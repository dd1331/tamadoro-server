package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "music_tracks")
data class MusicTrackEntity(
    @Id
    @Column(name = "id", nullable = false, length = 64)
    val id: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "theme", nullable = false)
    val theme: String,

    @Column(name = "is_premium", nullable = false)
    val isPremium: Boolean,

    @Column(name = "url", nullable = false)
    val url: String,

    @Column(name = "resource", nullable = false)
    val resource: String,
)
