package com.hobos.tamadoro.domain.collections

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

    @Column(name = "active_background_id")
    var activeBackgroundId: String? = null,

    @Column(name = "active_music_id")
    var activeMusicId: String? = null,

    @Column(name = "active_tama_id")
    var activeTamaId: String? = null,

    @Version
    @Column(name = "version")
    var version: Long? = 0,
)
