package com.hobos.tamadoro.domain.tamas.entity

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
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
    var activeBackground: BackgroundEntity? = null,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_tama_id", nullable = false)
    var activeTama: TamaCatalogEntity? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    )
