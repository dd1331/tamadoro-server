package com.hobos.tamadoro.domain.task

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

/**
 * Repository interface for Task entity.
 */
@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    /**
     * Find all tasks for a user.
     */
    fun findByUserId(userId: UUID): List<Task>
    
    /**
     * Find all tasks for a user with a specific completion status.
     */
    fun findByUserIdAndCompleted(userId: UUID, completed: Boolean): List<Task>
    
    /**
     * Find all tasks for a user with a specific priority.
     */
    fun findByUserIdAndPriority(userId: UUID, priority: TaskPriority): List<Task>
    
    /**
     * Find all tasks for a user created within a date range.
     */
    fun findByUserIdAndCreatedAtBetween(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Task>
    
    /**
     * Find all tasks for a user completed within a date range.
     */
    fun findByUserIdAndCompletedIsTrueAndCompletedAtBetween(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Task>
    
    /**
     * Count the number of completed tasks for a user within a date range.
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.completed = true AND t.completedAt BETWEEN :startDate AND :endDate")
    fun countCompletedTasksByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long
    
    /**
     * Calculate the total estimated pomodoros for a user's incomplete tasks.
     */
    @Query("SELECT SUM(t.estimatedPomodoros) FROM Task t WHERE t.user.id = :userId AND t.completed = false")
    fun sumEstimatedPomodorosForIncompleteTasksByUserId(userId: UUID): Int?
    
    /**
     * Calculate the total completed pomodoros for a user within a date range.
     */
    @Query("SELECT SUM(t.completedPomodoros) FROM Task t WHERE t.user.id = :userId AND t.completedPomodoros > 0 AND t.updatedAt BETWEEN :startDate AND :endDate")
    fun sumCompletedPomodorosByUserIdAndDateRange(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int?
}