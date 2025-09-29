package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "backgrounds")
 class BackgroundEntity(theme: String, title: String, url: String) : ItemEntity(theme,title, url)
