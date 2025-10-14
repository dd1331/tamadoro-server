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
    private val userRepository: UserRepository,
    private val userProgressAssembler: UserProgressAssembler
) {
    /**
     * Gets a user's profile.
     */
    fun getUserProfile(userId: UUID): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val progress = userProgressAssembler.assemble(user.id)
        return UserDto.fromEntity(user, progress = progress)
    }
    
    /**
     * Updates a user's profile.
     */
    @Transactional
    fun updateUserProfile(userId: UUID, request: UpdateUserProfileRequest): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }


        val updatedUser = userRepository.save(user)
        val progress = userProgressAssembler.assemble(updatedUser.id)
        return UserDto.fromEntity(updatedUser, progress = progress)
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
    val providerId: String,
    val isPremium: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String?,
    val subscription: SubscriptionDto?,
    val progress: UserProgressDto?
) {
    companion object {
        fun fromEntity(entity: User, progress: UserProgressDto? = null): UserDto {
            val latest = entity.subscriptions
                .sortedByDescending { it.startDate }
                .firstOrNull()
            return UserDto(
                id = entity.id,
                providerId = entity.providerId,
                isPremium = entity.hasPremium(),
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString(),
                lastLoginAt = entity.lastLoginAt?.toString(),
                subscription = latest?.let { SubscriptionDto.fromEntity(it) },
                progress = progress
            )
        }
    }
}

data class UserProgressDto(
    val tamas: List<TamaProgressDto>,
    val activeTamaId: String?,
    val careItems: CareItemsDto
)

data class TamaProgressDto(
    val id: Long,
    val tamaCatalogId: Long?,
    val name: String?,
    val experience: Int,
    val happiness: Int,
    val energy: Int,
    val hunger: Int,
    val isActive: Boolean
)

data class CareItemsDto(
    val food: Int,
    val toy: Int,
    val snack: Int
)

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
