package com.hobos.tamadoro.domain.collections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "backgrounds")
 class BackgroundEntity(
   theme: String = "test", title: String, url: String,

   @Column(name = "user_id") val userId: UUID? = null,

   @Column(name = "is_custom") val isCustom: Boolean? = false
 ) : ItemEntity(theme,title, url)
