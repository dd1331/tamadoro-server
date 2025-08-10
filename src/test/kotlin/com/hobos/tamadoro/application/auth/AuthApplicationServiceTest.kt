package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthApplicationServiceTest {

    @Mock
    private lateinit var authService: AuthService

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var authApplicationService: AuthApplicationService

    @Captor
    private lateinit var userCaptor: ArgumentCaptor<User>

    private lateinit var user: User
    private lateinit var appleAuthRequest: AppleAuthRequest

    @BeforeEach
    fun setUp() {
        user = User(
            id = UUID.randomUUID(),
            email = "test@example.com",
            name = "Test User"
        )

        appleAuthRequest = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
                email = "test@example.com",
                name = AppleUserName(
                    firstName = "Test",
                    lastName = "User"
                )
            )
        )
    }

    @Test
    fun `should authenticate with Apple and create new user`() {
        // Given
        val token = "jwt.token"
        val refreshToken = "refresh.token"

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty())
        `when`(userRepository.save(any())).thenReturn(user)
        lenient().`when`(authService.generateToken(any())).thenReturn(token)
        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequest)

        // Then
        assertNotNull(result)
        assertEquals(user.id, result.user.id)
        assertEquals("test@example.com", result.user.email)
        assertEquals("Test User", result.user.name)
        assertEquals(token, result.token)
        assertEquals(refreshToken, result.refreshToken)

        verify(userRepository, atLeastOnce()).save(any())
    }

    @Test
    fun `should authenticate with Apple and find existing user`() {
        // Given
        val token = "jwt.token"
        val refreshToken = "refresh.token"

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user))
        `when`(userRepository.save(any())).thenReturn(user)
        lenient().`when`(authService.generateToken(any())).thenReturn(token)
        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequest)

        // Then
        assertNotNull(result)
        assertEquals(user.id, result.user.id)
        assertEquals(token, result.token)
        assertEquals(refreshToken, result.refreshToken)

        verify(userRepository).save(user) // Should save to update lastLoginAt
    }

    @Test
    fun `should refresh token successfully`() {
        // Given
        val refreshToken = "refresh.token"
        val newToken = "new.jwt.token"
        val newRefreshToken = "new.refresh.token"

        `when`(authService.validateRefreshToken(refreshToken)).thenReturn(user.id)
        `when`(userRepository.findById(user.id)).thenReturn(Optional.of(user))
        lenient().`when`(authService.generateToken(any())).thenReturn(newToken)
        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(newRefreshToken)

        // When
        val result = authApplicationService.refreshToken(refreshToken)

        // Then
        assertNotNull(result)
        assertEquals(user.id, result.user.id)
        assertEquals(newToken, result.token)
        assertEquals(newRefreshToken, result.refreshToken)
    }

    @Test
    fun `should throw exception when user not found during refresh`() {
        // Given
        val refreshToken = "refresh.token"
        val userId = UUID.randomUUID()

        `when`(authService.validateRefreshToken(refreshToken)).thenReturn(userId)
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(NoSuchElementException::class.java) {
            authApplicationService.refreshToken(refreshToken)
        }
    }

    @Test
    fun `should logout successfully`() {
        // Given
        val token = "jwt.token"

        // When
        authApplicationService.logout(token)

        // Then
        verify(authService).invalidateToken(token)
    }

    @Test
    fun `should handle Apple user without email`() {
        // Given
        val appleAuthRequestWithoutEmail = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
                email = null,
                name = AppleUserName(
                    firstName = "Test",
                    lastName = "User"
                )
            )
        )

        val token = "jwt.token"
        val refreshToken = "refresh.token"
        val expectedEmail = "apple.user.id@apple.com"

        `when`(userRepository.findByEmail("")).thenReturn(Optional.empty())
        `when`(userRepository.save(any())).thenAnswer { invocation ->
            val savedUser = invocation.arguments[0] as User
            assertEquals(expectedEmail, savedUser.email)
            savedUser
        }
        lenient().`when`(authService.generateToken(any())).thenReturn(token)
        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequestWithoutEmail)

        // Then
        assertNotNull(result)
        assertEquals(expectedEmail, result.user.email)
        assertEquals("Test User", result.user.name)
    }

    @Test
    fun `should handle Apple user without name`() {
        // Given
        val appleAuthRequestWithoutName = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
                email = "test@example.com",
                name = null
            )
        )

        val token = "jwt.token"
        val refreshToken = "refresh.token"
        val expectedName = "Apple User"

        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty())
        `when`(userRepository.save(any())).thenAnswer { invocation ->
            val savedUser = invocation.arguments[0] as User
            assertEquals(expectedName, savedUser.name)
            savedUser
        }
        `when`(authService.generateToken(user.id)).thenReturn(token)
        `when`(authService.generateRefreshToken(user.id)).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequestWithoutName)

        // Then
        assertNotNull(result)
        assertEquals(expectedName, result.user.name)
    }
} 