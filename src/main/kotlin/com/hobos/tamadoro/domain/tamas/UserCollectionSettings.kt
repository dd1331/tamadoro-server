package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "user_collection_settings")
class UserCollectionSettings(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_background_id", nullable = false)
    var backgroundEntity: BackgroundEntity? = null,

    @Column(name = "active_music_id")
    var activeMusicId: Long? = null,

    @Column(name = "active_tama_id")
    var activeTamaId: Long? = null,

)
