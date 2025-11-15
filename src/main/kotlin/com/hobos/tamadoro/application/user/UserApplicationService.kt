package com.hobos.tamadoro.application.user

import com.hobos.tamadoro.application.tama.TamaDto
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for user-related use cases.
 */
@Service
class UserApplicationService(
    private val userRepository: UserRepository,
    private val userProgressAssembler: UserProgressAssembler,
    private val userTamaRepository: UserTamaRepository,
    private val userCollectionSettingsRepository: UserCollectionSettingsRepository
) {
    /**
     * Gets a user's profile.
     */
    @Transactional(readOnly = true)
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

    /**
     * Gets user's ranking information.
     */
    @Transactional(readOnly = true)
    fun getMyRanking(userId: UUID): UserRankDto {
        // Get user's active tama
        val activeTama = userTamaRepository.findOneByUserIdAndIsActiveTrue(userId)
            ?: throw NoSuchElementException("No active tama found for user: $userId")

        // Get user's rank by counting tamas with higher experience
        val rank = userTamaRepository.countByExperienceGreaterThan(activeTama.experience) + 1

        val setting = userCollectionSettingsRepository.findOneByUserId(userId)
        // Convert to TamaDto
        val tamaDto = TamaDto(
            id = activeTama.id,
            tamaCatalogId = activeTama.catalog.id,
            url = activeTama.catalog.url,
            name = activeTama.name,
            experience = activeTama.experience,
            isActive = activeTama.isActive,
            isOwned = true,
            backgroundUrl = setting.orElseThrow().activeBackground?.url
        )

        return UserRankDto(
            rank = rank,
            tama = tamaDto
        )
    }

    @Transactional(readOnly = true)
    fun getMyGroupRanking(userId: UUID): UserGroupRankDto {
        val activeTama = userTamaRepository.findOneByUserIdAndIsActiveTrue(userId)
            ?: throw NoSuchElementException("No active tama found for user: $userId")

        val groupId = userTamaRepository.findGroupIdByUserTamaId(activeTama.id)
            ?: throw NoSuchElementException("Active tama is not assigned to any group")

        val groupRanks = userTamaRepository.findGroupRanking(Pageable.unpaged()).content
        val position = groupRanks.indexOfFirst { it.groupId == groupId }
        if (position < 0) {
            throw IllegalStateException("Group ranking data not found for group: $groupId")
        }
        val rank = position + 1L

        val projection = groupRanks[position]
        val groupDto = GroupRankSummaryDto(
            id = projection.groupId,
            name = projection.groupName,
            countryCode = projection.country.name,
            experience = projection.totalExperience,
            url = projection.avatar,
            backgroundUrl = projection.background,
            memberCount = projection.tamaCount
        )

        return UserGroupRankDto(
            rank = rank,
            group = groupDto
        )
    }
}

data class UpdateUserProfileRequest(
    val email: String? = null,
    val name: String? = null
)

/**
 * DTO for user ranking information.
 */
data class UserRankDto(
    val rank: Long,
    val tama: TamaDto
)

data class UserGroupRankDto(
    val rank: Long,
    val group: GroupRankSummaryDto
)

data class GroupRankSummaryDto(
    val id: Long,
    val name: String,
    val countryCode: String,
    val experience: Long,
    val url: String?,
    val backgroundUrl: String?,
    val memberCount: Long
)

/**
 * DTO for user data.
 */
data class UserDto(
    val id: UUID,
    val providerId: String,
    val countryCode: String,
    val isPremium: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String?,
    val subscription: SubscriptionDto?,
    val progress: UserProgressDto?
) {
    companion object {
        fun fromEntity(entity: User, progress: UserProgressDto? = null): UserDto {
            println("eeee"+ entity.subscriptions.toString())
            val latest = entity.subscriptions
                .sortedByDescending { it.startDate }
                .firstOrNull()
            return UserDto(
                id = entity.id,
                providerId = entity.providerId,
                countryCode = entity.country.name,
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
    val activeTamaId: Long?,
)

data class TamaProgressDto(
    val id: Long,
    val tamaCatalogId: Long?,
    val name: String?,
    val experience: Int,
    val isActive: Boolean
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
