package com.hobos.tamadoro.domain.tama

import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a tama virtual pet.
 */
@Entity
@Table(name = "tamas")
class Tama(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "tama_catalog_id", nullable = false)
    val tamaCatalogEntity: TamaCatalogEntity,

    @Column(name = "name", nullable = false)
    var name: String,
    
) {
}