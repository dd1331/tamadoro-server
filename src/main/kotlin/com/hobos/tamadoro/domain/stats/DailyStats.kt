package com.hobos.tamadoro.domain.stats

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

/**
 * Entity representing a user's daily statistics.
 */
@Entity
@Table(name = "daily_stats", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "date"])])
class DailyStats(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "date", nullable = false)
    val date: LocalDate,
    
    @Column(name = "completed_pomodoros", nullable = false)
    var completedPomodoros: Int = 0,
    
    @Column(name = "total_focus_time", nullable = false)
    var totalFocusTime: Int = 0, // In minutes
    
    @Column(name = "completed_tasks", nullable = false)
    var completedTasks: Int = 0,
    
    @Column(name = "attendance", nullable = false)
    var attendance: Boolean = false,
    
    @Column(name = "coins_earned", nullable = false)
    var coinsEarned: Int = 0,
    
    @Column(name = "gems_earned", nullable = false)
    var gemsEarned: Int = 0
) {
    /**
     * Increments the completed pomodoros count.
     */
    fun incrementCompletedPomodoros() {
        completedPomodoros++
    }
    
    /**
     * Adds focus time in minutes.
     */
    fun addFocusTime(minutes: Int) {
        if (minutes > 0) {
            totalFocusTime += minutes
        }
    }
    
    /**
     * Increments the completed tasks count.
     */
    fun incrementCompletedTasks() {
        completedTasks++
    }
    
    /**
     * Marks attendance for the day.
     */
    fun markAttendance() {
        attendance = true
    }
    
    /**
     * Adds earned coins.
     */
    fun addCoinsEarned(coins: Int) {
        if (coins > 0) {
            coinsEarned += coins
        }
    }
    
    /**
     * Adds earned gems.
     */
    fun addGemsEarned(gems: Int) {
        if (gems > 0) {
            gemsEarned += gems
        }
    }
    
    /**
     * Calculates the productivity score for the day.
     */
    fun calculateProductivityScore(): Int {
        // A simple formula that considers completed pomodoros, focus time, and completed tasks
        val pomodoroScore = completedPomodoros * 10
        val focusTimeScore = totalFocusTime / 5 // 1 point per 5 minutes
        val taskScore = completedTasks * 20
        
        return (pomodoroScore + focusTimeScore + taskScore).coerceAtMost(100)
    }
    
    /**
     * Checks if the user had a productive day (score >= 50).
     */
    fun isProductiveDay(): Boolean {
        return calculateProductivityScore() >= 50
    }
}