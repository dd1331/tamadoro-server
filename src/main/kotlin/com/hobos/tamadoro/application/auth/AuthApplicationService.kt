package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.application.user.UserDto
import com.hobos.tamadoro.application.user.UserProgressAssembler
import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.collections.BackgroundEntity
import com.hobos.tamadoro.domain.collections.BackgroundRepository
import com.hobos.tamadoro.domain.collections.MusicTrackEntity
import com.hobos.tamadoro.domain.collections.MusicTrackRepository
import com.hobos.tamadoro.domain.collections.UserTama
import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.UserCollectionSettings
import com.hobos.tamadoro.domain.collections.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Request for Apple Sign-In authentication.
 */
data class AppleAuthRequest(
    val identityToken: String,
    val authorizationCode: String? = null,
    val user: AppleUser
)

/**
 * Apple user information.
 */
data class AppleUser(
    val id: String,
    val email: String? = null,
    val name: AppleUserName? = null
)

/**
 * Apple user name information.
 */
data class AppleUserName(
    val firstName: String? = null,
    val lastName: String? = null
)

/**
 * Application service for authentication-related use cases.
 */
@Service
class AuthApplicationService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val userTamaRepository: UserTamaRepository,
    private val tamaCatalogRepository: TamaCatalogRepository,
    private val backgroundRepository: BackgroundRepository,
    private val musicTrackRepository: MusicTrackRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
    private val environment: Environment,
    private val userProgressAssembler: UserProgressAssembler
) {
    /**
     * Authenticates a user with Apple Sign-In.
     */
    @Transactional
    fun authenticateWithApple(request: AppleAuthRequest, currentUserId: UUID? = null): AuthResponse {
        // Validate Apple identity token (in a real implementation, this would verify with Apple)
        val appleUserId = request.user.id

        // Find or create user
        val user = userRepository.findByProviderId(appleUserId)
            .orElseGet {
                currentUserId
                    ?.let { mergeGuestWithApple(it, appleUserId) }
                    ?: createUserWithStarterTama(appleUserId)
            }


        // Record login
        user.recordLogin()
        userRepository.save(user)
        
        // Generate tokens
        val token = authService.generateToken(user.id)
        val refreshToken = authService.issueRefreshToken(user.id)
        
        val progress = userProgressAssembler.assemble(user.id)

        return AuthResponse(
            user = UserDto.fromEntity(user, progress),
            token = token,
            refreshToken = refreshToken
        )
    }

    /**
     * Issues guest credentials by creating a lightweight user account.
     */
    @Transactional
    fun loginAsGuest(): AuthResponse {
        val guestProviderId = "guest:${UUID.randomUUID()}"
        val user = createUserWithStarterTama(guestProviderId)

        user.recordLogin()
        userRepository.save(user)

        val token = authService.generateToken(user.id)
        val refreshToken = authService.issueRefreshToken(user.id)

        val progress = userProgressAssembler.assemble(user.id)

        return AuthResponse(
            user = UserDto.fromEntity(user, progress),
            token = token,
            refreshToken = refreshToken
        )
    }
    
    /**
     * Refreshes the authentication token.
     */
    fun refreshToken(refreshToken: String): TokenPair {
        val userId = authService.validateRefreshToken(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        
        val newToken = authService.generateToken(user.id)
        val newRefreshToken = authService.rotateRefreshToken(refreshToken)
        
        return TokenPair(
            token = newToken,
            refreshToken = newRefreshToken
        )
    }
    
    /**
     * Logs out the user.
     */
    fun logout(userId: UUID) {
        authService.logoutAll(userId)
    }

    private fun createUserWithStarterTama(providerId: String): User {
        val user = userRepository.save(User(providerId = providerId))

        val defaultCatalog = ensureDefaultTama()
        val defaultBackground = ensureDefaultBackground()
        val defaultMusic = ensureDefaultMusic()

        val starterTama = userTamaRepository.save(
            UserTama(
                user = user,
                tama = defaultCatalog,
                isActive = true
            )
        )

        val settings = userCollectionSettingsRepository.findByUser_Id(user.id)
            .orElseGet { UserCollectionSettings(user = user) }
        settings.activeBackgroundId = defaultBackground.id
        settings.activeMusicId = defaultMusic.id
        settings.activeTamaId = starterTama.tama.id
        userCollectionSettingsRepository.save(settings)

        return user
    }

    private fun ensureDefaultTama(): TamaCatalogEntity {
        return tamaCatalogRepository.findByIsPremium(isPremium = false)
            .firstOrNull()
            ?: tamaCatalogRepository.findAll().firstOrNull()
            ?: createDefaultTamaForTests()
    }

    private fun ensureDefaultBackground(): BackgroundEntity {
        val backgrounds = backgroundRepository.findAll()
        return backgrounds.firstOrNull { !it.isPremium }
            ?: backgrounds.firstOrNull()
            ?: createDefaultBackgroundForTests()
    }

    private fun ensureDefaultMusic(): MusicTrackEntity {
        val musicTracks = musicTrackRepository.findAll()
        return musicTracks.firstOrNull { !it.isPremium }
            ?: musicTracks.firstOrNull()
            ?: createDefaultMusicForTests()
    }

    private fun createDefaultTamaForTests(): TamaCatalogEntity {
        assertTestProfile()
        return tamaCatalogRepository.save(
            TamaCatalogEntity(
                theme = "classic",
                title = "Tamadoro",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
                isPremium = false
            )
        )
    }

    private fun createDefaultBackgroundForTests(): BackgroundEntity {
        assertTestProfile()
        return backgroundRepository.save(
            BackgroundEntity(
                theme = "default",
                title = "Cozy Room",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s"
            )
        )
    }

    private fun createDefaultMusicForTests(): MusicTrackEntity {
        assertTestProfile()
        return musicTrackRepository.save(
            MusicTrackEntity(
                resource = "https://tamadoro.app/assets/music/default.mp3",
                theme = "ambient",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
                title = "Gentle Focus"
            )
        )
    }

    private fun assertTestProfile() {
        val isTestProfile = environment.acceptsProfiles(Profiles.of("test")) ||
            environment.activeProfiles.contains("test")
        if (!isTestProfile) {
            throw IllegalStateException("Default starter assets must exist before onboarding users")
        }
    }

    private fun mergeGuestWithApple(userId: UUID, providerId: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("Guest user not found: $userId") }

        user.providerId = providerId
        return userRepository.save(user)
    }
}

/**
 * DTO for authentication response.
 */
data class AuthResponse(
    val user: UserDto,
    val token: String,
    val refreshToken: String
)

data class TokenPair(
    val token: String,
    val refreshToken: String
)
