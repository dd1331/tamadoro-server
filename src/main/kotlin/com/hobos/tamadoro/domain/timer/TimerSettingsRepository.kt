package com.hobos.tamadoro.domain.timer

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for TimerSettings entity.
 */
@Repository
interface TimerSettingsRepository : JpaRepository<TimerSettings, UUID> {
    /**
     * Find timer settings by user ID.
     */
    fun findByUserId(userId: UUID): Optional<TimerSettings>
    
    /**
     * Check if timer settings exist for a user.
     */
    fun existsByUserId(userId: UUID): Boolean
}