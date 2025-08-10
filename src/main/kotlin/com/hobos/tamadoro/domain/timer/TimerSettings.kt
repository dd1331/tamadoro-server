package com.hobos.tamadoro.domain.timer

import com.hobos.tamadoro.domain.user.User
import jakarta.persistence.*
import java.util.UUID

/**
 * Entity representing a user's timer settings.
 */
@Entity
@Table(name = "timer_settings")
class TimerSettings(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "work_time", nullable = false)
    val workTime: Int = 25, // 25 minutes by default
    
    @Column(name = "short_break_time", nullable = false)
    var shortBreakTime: Int = 5, // 5 minutes by default
    
    @Column(name = "long_break_time", nullable = false)
    var longBreakTime: Int = 15, // 15 minutes by default
    
    @Column(name = "long_break_interval", nullable = false)
    var longBreakInterval: Int = 4, // After 4 work sessions
    
    @Column(name = "auto_start_breaks", nullable = false)
    var autoStartBreaks: Boolean = false,
    
    @Column(name = "auto_start_pomodoros", nullable = false)
    var autoStartPomodoros: Boolean = false,
    
    @Column(name = "sound_enabled", nullable = false)
    var soundEnabled: Boolean = true,
    
    @Column(name = "vibration_enabled", nullable = false)
    var vibrationEnabled: Boolean = true
) {
    /**
     * Updates the timer settings.
     */
    fun update(
        shortBreakTime: Int? = null,
        longBreakTime: Int? = null,
        longBreakInterval: Int? = null,
        autoStartBreaks: Boolean? = null,
        autoStartPomodoros: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null
    ) {
        shortBreakTime?.let { this.shortBreakTime = it }
        longBreakTime?.let { this.longBreakTime = it }
        longBreakInterval?.let { this.longBreakInterval = it }
        autoStartBreaks?.let { this.autoStartBreaks = it }
        autoStartPomodoros?.let { this.autoStartPomodoros = it }
        soundEnabled?.let { this.soundEnabled = it }
        vibrationEnabled?.let { this.vibrationEnabled = it }
    }
    
    /**
     * Gets the duration for a specific timer session type.
     */
    fun getDurationForSessionType(type: TimerSessionType): Int {
        return when (type) {
            TimerSessionType.WORK -> workTime
            TimerSessionType.SHORT_BREAK -> shortBreakTime
            TimerSessionType.LONG_BREAK -> longBreakTime
        }
    }
}