package com.hobos.tamadoro.domain.auth

import com.hobos.tamadoro.config.AuthProperties
import com.hobos.tamadoro.domain.user.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.Date
import java.util.UUID
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class AuthService(
    private val props: AuthProperties,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    private val secretKey by lazy {
        val rawInput: ByteArray = if (props.secret.startsWith("base64:")) {
            Base64.getDecoder().decode(props.secret.removePrefix("base64:"))
        } else props.secret.toByteArray(StandardCharsets.UTF_8)

        val keyBytes = if (rawInput.size < 32) {
            // Derive a 256-bit key from the provided secret using SHA-256 to satisfy JJWT requirements
            MessageDigest.getInstance("SHA-256").digest(rawInput)
        } else rawInput

        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(userId: UUID, jti: String = UUID.randomUUID().toString()): String {
        val now = LocalDateTime.now()
        val expiration = now.plusMinutes(props.accessTokenMinutes)
        return Jwts.builder()
            .setSubject(userId.toString())
            .setId(jti)
            .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
            .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    @Transactional
    fun issueRefreshToken(userId: UUID): String {
        val user = userRepository.findById(userId).orElseThrow()
        val now = LocalDateTime.now()
        val expiration = now.plusDays(props.refreshTokenDays)
        val jti = UUID.randomUUID().toString()

        val token = Jwts.builder()
            .setSubject(userId.toString())
            .setId(jti)
            .claim("type", "refresh")
            .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
            .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()

        // rotate: invalidate existing tokens for user (optional: keep last N)
        refreshTokenRepository.deleteByUser_Id(userId)
        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = token,
                jti = jti,
                expiresAt = expiration,
                revoked = false
            )
        )
        return token
    }

    fun validateToken(token: String): UUID {
        val claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
        return UUID.fromString(claims.subject)
    }

    fun validateRefreshToken(refreshToken: String): UUID {
        val claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(refreshToken).payload
        if (claims["type"] != "refresh") throw IllegalArgumentException("Invalid refresh token")
        val jti = claims.id ?: throw IllegalArgumentException("Missing jti")
        val stored = refreshTokenRepository.findByJti(jti).orElseThrow { IllegalArgumentException("Refresh not found") }
        if (stored.revoked) throw IllegalArgumentException("Refresh revoked")
        if (stored.token != refreshToken) throw IllegalArgumentException("Refresh mismatch")
        return UUID.fromString(claims.subject)
    }

    fun rotateRefreshToken(oldToken: String): String {
        val claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(oldToken).payload
        val jti = claims.id ?: throw IllegalArgumentException("Missing jti")
        val stored = refreshTokenRepository.findByJti(jti).orElseThrow { IllegalArgumentException("Refresh not found") }
        stored.revoked = true
        refreshTokenRepository.save(stored)
        return issueRefreshToken(UUID.fromString(claims.subject))
    }

    fun logoutAll(userId: UUID) {
        refreshTokenRepository.deleteByUser_Id(userId)
    }
}