package com.hobos.tamadoro.application.user

import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for user-related use cases.
 */
@Service
class UserApplicationService(
    private val userRepository: UserRepository
) {
    /**
     * Gets a user's profile.
     */
    fun getUserProfile(userId: UUID): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        return UserDto.fromEntity(user)
    }
    
    /**
     * Updates a user's profile.
     */
    @Transactional
    fun updateUserProfile(userId: UUID, request: UpdateUserProfileRequest): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        // For now, we accept email/name but do not persist as entity lacks fields.
        // This keeps API compatibility with the mobile app.
        val updatedUser = userRepository.save(user)
        return UserDto.fromEntity(updatedUser)
    }
}

data class UpdateUserProfileRequest(
    val email: String? = null,
    val name: String? = null
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
