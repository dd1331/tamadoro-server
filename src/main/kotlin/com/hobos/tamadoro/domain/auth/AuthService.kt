package com.hobos.tamadoro.domain.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Domain service for authentication-related business logic.
 */
@Service
class AuthService {
    
    private val secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    private val tokenExpirationMinutes = 60L
    private val refreshTokenExpirationDays = 30L
    
    private val invalidatedTokens = mutableSetOf<String>()
    
    /**
     * Generates a JWT token for a user.
     */
    fun generateToken(userId: UUID): String {
        val now = LocalDateTime.now()
        val expiration = now.plusMinutes(tokenExpirationMinutes)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
            .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
            .signWith(secretKey)
            .compact()
    }
    
    /**
     * Generates a refresh token for a user.
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = LocalDateTime.now()
        val expiration = now.plusDays(refreshTokenExpirationDays)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
            .setExpiration(Date.from(expiration.toInstant(ZoneOffset.UTC)))
            .claim("type", "refresh")
            .signWith(secretKey)
            .compact()
    }
    
    /**
     * Validates a JWT token and returns the user ID.
     */
    fun validateToken(token: String): UUID {
        if (invalidatedTokens.contains(token)) {
            throw IllegalArgumentException("Token has been invalidated")
        }
        
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
        
        return UUID.fromString(claims.subject)
    }
    
    /**
     * Validates a refresh token and returns the user ID.
     */
    fun validateRefreshToken(refreshToken: String): UUID {
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(refreshToken)
            .payload
        
        // Check if it's a refresh token
        if (claims["type"] != "refresh") {
            throw IllegalArgumentException("Invalid refresh token")
        }
        
        return UUID.fromString(claims.subject)
    }
    
    /**
     * Invalidates a token.
     */
    fun invalidateToken(token: String) {
        invalidatedTokens.add(token)
        
        // Clean up old invalidated tokens (keep only last 1000)
        if (invalidatedTokens.size > 1000) {
            invalidatedTokens.clear()
        }
    }
    
    /**
     * Extracts user ID from token without validation.
     * Use this only when you know the token is valid.
     */
    fun extractUserIdFromToken(token: String): UUID? {
        return try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            
            UUID.fromString(claims.subject)
        } catch (e: Exception) {
            null
        }
    }
} 