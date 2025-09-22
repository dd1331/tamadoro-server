package com.hobos.tamadoro.domain.timer

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a timer session.
 */
@Entity
@Table(name = "timer_sessions")
class TimerSession(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TimerSessionType,
    
    @Column(name = "duration", nullable = false)
    var duration: Int, // Duration in minutes
    
    @Column(name = "completed", nullable = false)
    var completed: Boolean = false,
    
    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,
    
    @Column(name = "task_id")
    var taskId: UUID? = null
) {
    /**
     * Completes the timer session.
     */
    fun complete(at: LocalDateTime? = null) {
        if (!completed) {
            completed = true
        }
        completedAt = at ?: LocalDateTime.now()
    }

    fun markIncomplete() {
        if (completed) {
            completed = false
            completedAt = null
        }
    }
    
    /**
     * Calculates the actual duration of the session in minutes.
     */
    fun calculateActualDuration(): Int {
        val end = completedAt ?: LocalDateTime.now()
        return Duration.between(startedAt, end).toMinutes().toInt()
    }
    
    /**
     * Checks if the session is currently active.
     */
    fun isActive(): Boolean {
        return !completed && Duration.between(startedAt, LocalDateTime.now()).toMinutes() < duration
    }
    
    /**
     * Calculates the remaining time in minutes.
     */
    fun remainingTime(): Int {
        if (completed) return 0
        
        val elapsedMinutes = Duration.between(startedAt, LocalDateTime.now()).toMinutes().toInt()
        return (duration - elapsedMinutes).coerceAtLeast(0)
    }
    
    /**
     * Associates the session with a task.
     */
    fun associateWithTask(taskId: UUID) {
        this.taskId = taskId
    }
}
