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
    fun updateUserProfile(userId: UUID, request: UpdateUserRequest): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        user.updateProfile(
            name = request.name,
            email = request.email
        )
        
        val updatedUser = userRepository.save(user)
        return UserDto.fromEntity(updatedUser)
    }
}

/**
 * DTO for user data.
 */
data class UserDto(
    val id: UUID,
    val email: String,
    val name: String,
    val isPremium: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String?,
    val subscription: SubscriptionDto?
) {
    companion object {
        fun fromEntity(entity: User): UserDto {
            return UserDto(
                id = entity.id,
                email = entity.email,
                name = entity.name,
                isPremium = entity.isPremium,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString(),
                lastLoginAt = entity.lastLoginAt?.toString(),
                subscription = entity.subscription?.let { SubscriptionDto.fromEntity(it) }
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
    val endDate: String,
    val status: String
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.user.Subscription): SubscriptionDto {
            return SubscriptionDto(
                type = entity.type.name,
                startDate = entity.startDate.toString(),
                endDate = entity.endDate.toString(),
                status = entity.status.name
            )
        }
    }
}

/**
 * Request for updating user profile.
 */
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null
) 