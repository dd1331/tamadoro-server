package com.hobos.tamadoro.domain.timer

import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain service for timer-related business logic.
 */
@Service
class TimerService(
    private val timerSessionRepository: TimerSessionRepository,
    private val timerSettingsRepository: TimerSettingsRepository
) {
    /**
     * Creates a new timer session for a user.
     */
    @Transactional
    fun createTimerSession(
        user: User,
        type: TimerSessionType,
        durationOverride: Int? = null,
        startedAt: LocalDateTime? = null,
        completed: Boolean = false,
        completedAt: LocalDateTime? = null
    ): TimerSession {
        // Get user's timer settings or create default settings
        val settings = timerSettingsRepository.findByUserId(user.id)
            .orElseGet { createDefaultTimerSettings(user) }
        
        // Create a new timer session
        val duration = durationOverride ?: settings.getDurationForSessionType(type)
        val session = TimerSession(
            user = user,
            type = type,
            duration = duration,
            startedAt = startedAt ?: LocalDateTime.now(),
            completed = false,
            completedAt = null
        )
        if (completed || completedAt != null) {
            session.complete(completedAt)
        }
        
        return timerSessionRepository.save(session)
    }
    
    /**
     * Completes a timer session and updates related entities.
     */
    @Transactional
    fun completeTimerSession(sessionId: UUID): TimerSession {
        val session = timerSessionRepository.findById(sessionId)
            .orElseThrow { NoSuchElementException("Timer session not found with ID: $sessionId") }
        
        if (session.completed) {
            return session // Already completed
        }
        
        // Complete the session
        session.complete()

        return timerSessionRepository.save(session)
    }

    @Transactional
    fun updateTimerSession(
        sessionId: UUID,
        duration: Int? = null,
        completed: Boolean? = null,
        completedAt: LocalDateTime? = null,
        startedAt: LocalDateTime? = null
    ): TimerSession {
        val session = timerSessionRepository.findById(sessionId)
            .orElseThrow { NoSuchElementException("Timer session not found with ID: $sessionId") }

        duration?.let { session.duration = it }
        startedAt?.let { session.startedAt = it }

        completed?.let {
            if (it) {
                session.complete(completedAt)
            } else {
                session.markIncomplete()
            }
        }

        if (completedAt != null && completed == null) {
            session.complete(completedAt)
        }

        return timerSessionRepository.save(session)
    }
    
    /**
     * Updates a user's timer settings.
     */
    @Transactional
    fun updateTimerSettings(
        userId: UUID,
        workTime: Int? = null,
        shortBreakTime: Int? = null,
        longBreakTime: Int? = null,
        longBreakInterval: Int? = null,
        autoStartBreaks: Boolean? = null,
        autoStartPomodoros: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ): TimerSettings {
        val settings = timerSettingsRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Timer settings not found for user ID: $userId") }
        
        settings.update(
            workTime = workTime,
            shortBreakTime = shortBreakTime,
            longBreakTime = longBreakTime,
            longBreakInterval = longBreakInterval,
            autoStartBreaks = autoStartBreaks,
            autoStartPomodoros = autoStartPomodoros,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            notificationEnabled = notificationsEnabled,
        )
        
        return timerSettingsRepository.save(settings)
    }
    
    /**
     * Gets a user's timer settings or creates default settings if none exist.
     */
    @Transactional
    fun getOrCreateTimerSettings(user: User): TimerSettings {
        return timerSettingsRepository.findByUserId(user.id)
            .orElseGet { createDefaultTimerSettings(user) }
    }
    
    /**
     * Gets all timer sessions for a user within a date range.
     */
    fun getTimerSessionsByDateRange(
        userId: UUID,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<TimerSession> {
        return timerSessionRepository.findByUserIdAndStartedAtBetween(userId, startDate, endDate)
    }
    
    /**
     * Gets all completed work sessions for a user on a specific date.
     */
    fun getCompletedWorkSessionsByDate(userId: UUID, date: LocalDate): List<TimerSession> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1)
        
        return timerSessionRepository.findByUserIdAndStartedAtBetween(userId, startOfDay, endOfDay)
            .filter { it.completed && it.type == TimerSessionType.WORK }
    }

    fun findById(sessionId: UUID): TimerSession =
        timerSessionRepository.findById(sessionId)
            .orElseThrow { NoSuchElementException("Timer session not found with ID: $sessionId") }
    
    /**
     * Calculates the total focus time for a user on a specific date.
     */
    fun calculateTotalFocusTimeForDate(userId: UUID, date: LocalDate): Int {
        val sessions = getCompletedWorkSessionsByDate(userId, date)
        return sessions.sumOf { it.calculateActualDuration() }
    }
    
    /**
     * Creates default timer settings for a user.
     */
    private fun createDefaultTimerSettings(user: User): TimerSettings {
        val settings = TimerSettings(user = user)
        return timerSettingsRepository.save(settings)
    }
    
}
