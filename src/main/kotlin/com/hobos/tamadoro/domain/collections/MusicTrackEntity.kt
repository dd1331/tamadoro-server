package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "music_tracks")
 class MusicTrackEntity(
    @Column(name = "resource", nullable = false)
    val resource: String,

    theme: String,
    url: String,
    title: String,


    ): ItemEntity(theme,title,url)
