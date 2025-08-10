package com.hobos.tamadoro.domain.timer

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository interface for TimerSession entity.
 */
@Repository
interface TimerSessionRepository : JpaRepository<TimerSession, UUID> {
    /**
     * Find all timer sessions for a user.
     */
    fun findByUserId(userId: UUID): List<TimerSession>
    
    /**
     * Find all completed timer sessions for a user.
     */
    fun findByUserIdAndCompleted(userId: UUID, completed: Boolean): List<TimerSession>
    
    /**
     * Find all timer sessions for a user within a date range.
     */
    fun findByUserIdAndStartedAtBetween(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<TimerSession>
    
    /**
     * Find all timer sessions for a user associated with a specific task.
     */
    fun findByUserIdAndTaskId(userId: UUID, taskId: UUID): List<TimerSession>
    
    /**
     * Count the number of completed work sessions for a user within a date range.
     */
    @Query("SELECT COUNT(ts) FROM TimerSession ts WHERE ts.user.id = :userId AND ts.type = 'WORK' AND ts.completed = true AND ts.startedAt BETWEEN :startDate AND :endDate")
    fun countCompletedWorkSessionsByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long
    
    /**
     * Calculate the total focus time (in minutes) for a user within a date range.
     */
    @Query("SELECT SUM(ts.duration) FROM TimerSession ts WHERE ts.user.id = :userId AND ts.type = 'WORK' AND ts.completed = true AND ts.startedAt BETWEEN :startDate AND :endDate")
    fun sumFocusTimeByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int?
}