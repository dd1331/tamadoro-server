package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.application.user.CareItemsDto
import com.hobos.tamadoro.application.user.UserProgressAssembler
import com.hobos.tamadoro.application.user.UserProgressDto
import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.collections.BackgroundEntity
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.MusicTrackEntity
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.UserCollectionSettings
import com.hobos.tamadoro.domain.collections.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tama.UserTamaRepository
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
import org.springframework.core.env.Environment
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthApplicationServiceTest {

    @Mock
    private lateinit var authService: AuthService

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userTamaRepository: UserTamaRepository

    @Mock
    private lateinit var tamaCatalogRepository: TamaCatalogRepository

    @Mock
    private lateinit var backgroundRepository: BackgroundRepository

    @Mock
    private lateinit var musicTrackRepository: MusicTrackRepository

    @Mock
    private lateinit var userCollectionSettingsRepository: UserCollectionSettingsRepository

    @Mock
    private lateinit var environment: Environment

    @Mock
    private lateinit var userProgressAssembler: UserProgressAssembler

    @InjectMocks
    private lateinit var authApplicationService: AuthApplicationService

    @Captor
    private lateinit var userCaptor: ArgumentCaptor<User>

    private lateinit var user: User
    private lateinit var appleAuthRequest: AppleAuthRequest

    private lateinit var defaultTama: TamaCatalogEntity
    private lateinit var defaultBackground: BackgroundEntity
    private lateinit var defaultMusic: MusicTrackEntity

    @BeforeEach
    fun setUp() {
        user = User(
            id = UUID.randomUUID(),
            providerId = UUID.randomUUID().toString()
        )

        defaultTama = TamaCatalogEntity(
            theme = "classic",
            title = "Default Tama",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10",
            isPremium = false
        )
        defaultBackground = BackgroundEntity(
            theme = "default",
            title = "Cozy Room",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
        )
        defaultMusic = MusicTrackEntity(
            resource = "https://example.com/music.mp3",
            theme = "ambient",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10",
            title = "Gentle Focus"
        )

        lenient().`when`(environment.activeProfiles).thenReturn(arrayOf("test"))

        lenient().`when`(tamaCatalogRepository.findByIsPremium(false)).thenReturn(listOf(defaultTama))
        lenient().`when`(backgroundRepository.findAll()).thenReturn(listOf(defaultBackground))
        lenient().`when`(musicTrackRepository.findAll()).thenReturn(listOf(defaultMusic))
        lenient().`when`(userCollectionSettingsRepository.findByUser_Id(any())).thenReturn(Optional.empty())
        lenient().`when`(userCollectionSettingsRepository.save(any())).thenAnswer { it.arguments[0] as UserCollectionSettings }
        lenient().`when`(userTamaRepository.save(any())).thenAnswer { it.arguments[0] }
        lenient().`when`(userProgressAssembler.assemble(any())).thenReturn(
            UserProgressDto(
                tamas = emptyList(),
                activeTamaId = null,
                careItems = CareItemsDto(food = 0, toy = 0, snack = 0)
            )
        )

        appleAuthRequest = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
            )
        )
    }

    @Test
    fun `should authenticate with Apple and create new user`() {
        // Given
        val token = "jwt.token"
        val refreshToken = "refresh.token"

        `when`(userRepository.save(any())).thenReturn(user)
        lenient().`when`(authService.generateToken(any())).thenReturn(token)
//        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequest)

        // Then
        assertNotNull(result)
        assertEquals(user.id, result.user.id)
        assertEquals(token, result.token)
        assertEquals(refreshToken, result.refreshToken)

        verify(userRepository, atLeastOnce()).save(any())
    }

    @Test
    fun `should authenticate with Apple and find existing user`() {
        // Given
        val token = "jwt.token"
        val refreshToken = "refresh.token"

        `when`(userRepository.save(any())).thenReturn(user)
        lenient().`when`(authService.generateToken(any())).thenReturn(token)
//        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

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
//        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(newRefreshToken)

        // When
        val result = authApplicationService.refreshToken(refreshToken)

        // Then
        assertNotNull(result)
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
        authApplicationService.logout(
            userId = user.id,
        )

        // Then
//        verify(authService).invalidateToken(token)
    }

    @Test
    fun `should handle Apple user without email`() {
        // Given
        val appleAuthRequestWithoutEmail = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
            )
        )

        val token = "jwt.token"
        val refreshToken = "refresh.token"
        val expectedEmail = "apple.user.id@apple.com"

        lenient().`when`(authService.generateToken(any())).thenReturn(token)
//        lenient().`when`(authService.generateRefreshToken(any())).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequestWithoutEmail)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `should handle Apple user without name`() {
        // Given
        val appleAuthRequestWithoutName = AppleAuthRequest(
            identityToken = "test.identity.token",
            authorizationCode = "test.auth.code",
            user = AppleUser(
                id = "apple.user.id",
            )
        )

        val token = "jwt.token"
        val refreshToken = "refresh.token"
        val expectedName = "Apple User"

        `when`(userRepository.save(any())).thenAnswer { invocation ->
            val savedUser = invocation.arguments[0] as User
            savedUser
        }
        `when`(authService.generateToken(user.id)).thenReturn(token)
//        `when`(authService.generateRefreshToken(user.id)).thenReturn(refreshToken)

        // When
        val result = authApplicationService.authenticateWithApple(appleAuthRequestWithoutName)

        // Then
        assertNotNull(result)
    }
} 
