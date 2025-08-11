package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.domain.auth.AuthService
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Request for Apple Sign-In authentication.
 */
data class AppleAuthRequest(
    val identityToken: String,
    val authorizationCode: String,
    val user: AppleUser
)

/**
 * Apple user information.
 */
data class AppleUser(
    val id: String,
)

/**
 * Apple user name information.
 */
data class AppleUserName(
    val firstName: String?,
    val lastName: String?
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
            .orElseGet {
                val newUser = User(providerId = appleUserId)
                userRepository.save(newUser)
            }
        
        // Record login
        user.recordLogin()
        userRepository.save(user)
        
        // Generate tokens
        val token = authService.generateToken(user.id)
        val refreshToken = authService.generateRefreshToken(user.id)
        
        return AuthResponse(
            user = UserDto.fromEntity(user),
            token = token,
            refreshToken = refreshToken
        )
    }
    
    /**
     * Refreshes the authentication token.
     */
    fun refreshToken(refreshToken: String): AuthResponse {
        val userId = authService.validateRefreshToken(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found") }
        
        val newToken = authService.generateToken(user.id)
        val newRefreshToken = authService.generateRefreshToken(user.id)
        
        return AuthResponse(
            user = UserDto.fromEntity(user),
            token = newToken,
            refreshToken = newRefreshToken
        )
    }
    
    /**
     * Logs out the user.
     */
    fun logout(token: String) {
        authService.invalidateToken(token)
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

/**
 * DTO for user data.
 */
data class UserDto(
    val id: UUID,
    val isPremium: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String?,
    val subscription: SubscriptionDto?
) {
    companion object {
        fun fromEntity(entity: User): UserDto {
            val latest = entity.subscriptions
                .sortedByDescending { it.startDate }
                .firstOrNull()
            return UserDto(
                id = entity.id,
                isPremium = entity.isPremium,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString(),
                lastLoginAt = entity.lastLoginAt?.toString(),
                subscription = latest?.let { SubscriptionDto.fromEntity(it) }
            )
        }
    }
}

/**
 * DTO for subscription data.
 */
data class SubscriptionDto(
    val type: String,
    val startDate: String,
    val endDate: String?,
    val status: String
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.user.Subscription): SubscriptionDto {
            return SubscriptionDto(
                type = entity.type.name,
                startDate = entity.startDate.toString(),
                endDate = entity.endDate?.toString(),
                status = entity.status.name
            )
        }
    }
} 