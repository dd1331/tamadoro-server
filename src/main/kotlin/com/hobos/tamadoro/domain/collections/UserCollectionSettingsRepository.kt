package com.hobos.tamadoro.domain.collections

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserCollectionSettingsRepository : JpaRepository<UserCollectionSettings, UUID> {
    fun findByUser_Id(userId: UUID): Optional<UserCollectionSettings>

    fun findByUser_IdIn(userIds: Collection<UUID>): List<UserCollectionSettings>
}
