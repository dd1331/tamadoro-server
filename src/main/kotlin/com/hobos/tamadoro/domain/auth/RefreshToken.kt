package com.hobos.tamadoro.domain.auth

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens", indexes = [Index(name = "idx_user", columnList = "user_id")])
class RefreshToken(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "token", nullable = false, unique = true, length = 1024)
    var token: String,

    @Column(name = "jti", nullable = false, unique = true, length = 128)
    var jti: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime,

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false,
)
