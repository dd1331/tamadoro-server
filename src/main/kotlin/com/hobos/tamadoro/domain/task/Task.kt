package com.hobos.tamadoro.domain.task

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a task.
 */
@Entity
@Table(name = "tasks")
class Task(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "title", nullable = false)
    var title: String,
    
    @Column(name = "description")
    var description: String? = null,
    
    @Column(name = "completed", nullable = false)
    var completed: Boolean = false,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    var priority: TaskPriority = TaskPriority.MEDIUM,
    
    @Column(name = "estimated_pomodoros", nullable = false)
    var estimatedPomodoros: Int = 1,
    
    @Column(name = "completed_pomodoros", nullable = false)
    var completedPomodoros: Int = 0,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
) {
    /**
     * Updates the task details.
     */
    fun update(
        title: String? = null,
        description: String? = null,
        priority: TaskPriority? = null,
        estimatedPomodoros: Int? = null
    ) {
        title?.let { this.title = it }
        description?.let { this.description = it }
        priority?.let { this.priority = it }
        estimatedPomodoros?.let { this.estimatedPomodoros = it }
        this.updatedAt = LocalDateTime.now()
    }
    
    /**
     * Marks the task as completed.
     */
    fun complete() {
        if (!completed) {
            this.completed = true
            this.completedAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * Marks the task as incomplete.
     */
    fun reopen() {
        if (completed) {
            this.completed = false
            this.completedAt = null
            this.updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * Increments the completed pomodoros count.
     */
    fun incrementCompletedPomodoros() {
        this.completedPomodoros++
        this.updatedAt = LocalDateTime.now()
    }
    
    /**
     * Calculates the progress percentage of the task.
     */
    fun calculateProgress(): Int {
        if (estimatedPomodoros <= 0) return 0
        return ((completedPomodoros.toDouble() / estimatedPomodoros) * 100).toInt().coerceAtMost(100)
    }
    
    /**
     * Checks if the task is overdue based on the estimated completion date.
     */
    fun isOverdue(estimatedCompletionDate: LocalDateTime): Boolean {
        return !completed && LocalDateTime.now().isAfter(estimatedCompletionDate)
    }
}