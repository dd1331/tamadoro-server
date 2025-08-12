package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserCollectionOwnershipRepository : JpaRepository<UserCollectionOwnership, UUID> {
    fun existsByUser_IdAndCategoryAndItemId(userId: UUID, category: CollectionCategory, itemId: String): Boolean
    fun findByUser_IdAndCategory(userId: UUID, category: CollectionCategory): List<UserCollectionOwnership>
}
