package com.hobos.tamadoro.domain.stats

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * Repository interface for DailyStats entity.
 */
@Repository
interface DailyStatsRepository : JpaRepository<DailyStats, UUID> {
    /**
     * Find a user's daily stats for a specific date.
     */
    fun findByUserIdAndDate(userId: UUID, date: LocalDate): Optional<DailyStats>
    
    /**
     * Find all daily stats for a user within a date range.
     */
    fun findByUserIdAndDateBetweenOrderByDateAsc(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyStats>
    
    /**
     * Check if a user has stats for a specific date.
     */
    fun existsByUserIdAndDate(userId: UUID, date: LocalDate): Boolean
    
    /**
     * Count the number of consecutive days with attendance for a user up to a specific date.
     */
    @Query("""
        SELECT COUNT(ds)
        FROM DailyStats ds
        WHERE ds.user.id = :userId
        AND ds.attendance = true
        AND ds.date <= :endDate
        AND NOT EXISTS (
            SELECT 1
            FROM DailyStats gap
            WHERE gap.user.id = :userId
            AND gap.date < :endDate
            AND gap.date > ds.date
            AND gap.attendance = false
        )
        ORDER BY ds.date DESC
    """)
    fun countConsecutiveAttendanceDays(userId: UUID, endDate: LocalDate): Long
    
    /**
     * Calculate the total focus time for a user within a date range.
     */
    @Query("SELECT SUM(ds.totalFocusTime) FROM DailyStats ds WHERE ds.user.id = :userId AND ds.date BETWEEN :startDate AND :endDate")
    fun sumTotalFocusTimeByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int?
    
    /**
     * Calculate the total completed pomodoros for a user within a date range.
     */
    @Query("SELECT SUM(ds.completedPomodoros) FROM DailyStats ds WHERE ds.user.id = :userId AND ds.date BETWEEN :startDate AND :endDate")
    fun sumCompletedPomodorosByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int?
    
    /**
     * Calculate the total completed tasks for a user within a date range.
     */
    @Query("SELECT SUM(ds.completedTasks) FROM DailyStats ds WHERE ds.user.id = :userId AND ds.date BETWEEN :startDate AND :endDate")
    fun sumCompletedTasksByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Int?
    
    /**
     * Find the date with the highest focus time for a user within a date range.
     */
    @Query("SELECT ds FROM DailyStats ds WHERE ds.user.id = :userId AND ds.date BETWEEN :startDate AND :endDate ORDER BY ds.totalFocusTime DESC")
    fun findBestDayByFocusTime(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): Optional<DailyStats>
}