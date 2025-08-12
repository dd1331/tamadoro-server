package com.hobos.tamadoro.domain.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByJti(jti: String): Optional<RefreshToken>
    fun deleteByUser_Id(userId: UUID)
}
