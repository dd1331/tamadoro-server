package com.hobos.tamadoro.domain.tama

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
    
    @Column(name = "name", nullable = false)
    var name: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TamaType,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false)
    val rarity: TamaRarity,

) {
}