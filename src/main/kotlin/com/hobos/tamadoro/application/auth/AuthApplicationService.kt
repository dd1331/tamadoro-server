package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.application.user.UserDto
import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
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
    private val userRepository: UserRepository
) {
    /**
     * Authenticates a user with Apple Sign-In.
     */
    @Transactional
    fun authenticateWithApple(request: AppleAuthRequest): AuthResponse {
        // Validate Apple identity token (in a real implementation, this would verify with Apple)
        val appleUserId = request.user.id

        // Find or create user
        val user = userRepository.findByProviderId(appleUserId)
            .orElseGet { userRepository.save(User(providerId = appleUserId)) }

        request.user.name?.let { name ->
            val composedName = listOfNotNull(name.firstName, name.lastName)
                .joinToString(" ")
                .ifBlank { user.displayName ?: "" }
            if (composedName.isNotBlank()) {
                user.displayName = composedName
            }
        }

        // Record login
        user.recordLogin()
        userRepository.save(user)
        
        // Generate tokens
        val token = authService.generateToken(user.id)
        val refreshToken = authService.issueRefreshToken(user.id)
        
        return AuthResponse(
            user = UserDto.fromEntity(user),
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
