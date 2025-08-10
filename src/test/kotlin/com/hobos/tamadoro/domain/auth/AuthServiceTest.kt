package com.hobos.tamadoro.domain.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService()
    }

    @Test
    fun `should generate valid JWT token`() {
        // Given
        val userId = UUID.randomUUID()

        // When
        val token = authService.generateToken(userId)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        
        // Validate the token
        val extractedUserId = authService.validateToken(token)
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should generate valid refresh token`() {
        // Given
        val userId = UUID.randomUUID()

        // When
        val refreshToken = authService.generateRefreshToken(userId)

        // Then
        assertNotNull(refreshToken)
        assertTrue(refreshToken.isNotEmpty())
        
        // Validate the refresh token
        val extractedUserId = authService.validateRefreshToken(refreshToken)
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should invalidate token`() {
        // Given
        val userId = UUID.randomUUID()
        val token = authService.generateToken(userId)

        // When
        authService.invalidateToken(token)

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            authService.validateToken(token)
        }
    }

    @Test
    fun `should extract user ID from valid token`() {
        // Given
        val userId = UUID.randomUUID()
        val token = authService.generateToken(userId)

        // When
        val extractedUserId = authService.extractUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should return null for invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"

        // When
        val extractedUserId = authService.extractUserIdFromToken(invalidToken)

        // Then
        assertNull(extractedUserId)
    }

    @Test
    fun `should throw exception for invalid refresh token`() {
        // Given
        val userId = UUID.randomUUID()
        val regularToken = authService.generateToken(userId) // Not a refresh token

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            authService.validateRefreshToken(regularToken)
        }
    }

    @Test
    fun `should handle multiple token invalidations`() {
        // Given
        val userId = UUID.randomUUID()
        val token1 = authService.generateToken(userId)
        val token2 = authService.generateToken(userId)

        // When
        authService.invalidateToken(token1)
        authService.invalidateToken(token2)

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            authService.validateToken(token1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            authService.validateToken(token2)
        }
    }
} 