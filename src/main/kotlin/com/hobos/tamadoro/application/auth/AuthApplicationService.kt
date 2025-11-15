package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.application.user.UserDto
import com.hobos.tamadoro.application.user.UserProgressAssembler
import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.common.Country
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalogEntity
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.entity.UserCollectionSettings
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
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
    val user: AppleUser,
//    @field:NotBlank
    val countryCode: String
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

data class GuestLoginRequest(
    val countryCode: String
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
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository,
    private val environment: Environment,
    private val userProgressAssembler: UserProgressAssembler
) {
    /**
     * Authenticates a user with Apple Sign-In.
     */
    @Transactional
    fun authenticateWithApple(request: AppleAuthRequest): AuthResponse {
        // Validate Apple identity token (in a real implementation, this would verify with Apple)
        val appleUserId = request.user.id
        val country = Country.fromCode(request.countryCode)

        println("appleUserId +" + appleUserId)
        // Find or create user
        val user = userRepository.findByProviderId(appleUserId)
            .orElseGet {
                createUserWithStarterTama(appleUserId, country)
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

    private fun createUserWithStarterTama(providerId: String, country: Country): User {
        println("providerId" + providerId)
        val user = userRepository.save(User(providerId = providerId, country = country))

        val defaultCatalog = ensureDefaultTama()
        val defaultBackground = backgroundRepository.findByIsPremiumAndIsCustom(isPremium = false, isCustom = false)

        val starterTama = userTamaRepository.save(
            UserTama(
                user = user,
                tama = defaultCatalog,
                isActive = true
            )
        )
        userCollectionSettingsRepository.save(
            UserCollectionSettings(
            user = user,
            activeTama = starterTama.tama,
            activeBackground = defaultBackground.get(0)

        )
        )
        return user
    }

    private fun ensureDefaultTama(): TamaCatalogEntity {
        return tamaCatalogRepository.findByIsPremium(isPremium = false)
            .firstOrNull()
            ?: throw IllegalStateException("Default starter assets must exist before onboarding users")

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
