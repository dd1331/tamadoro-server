package com.hobos.tamadoro.application.timer

import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.timer.TimerService
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Application service for timer-related use cases.
 * This service coordinates multiple domain services to implement higher-level operations.
 */
@Service
class TimerApplicationService(
    private val timerService: TimerService,
    private val tamaService: TamaService,
    private val userRepository: UserRepository
) {
    /**
     * Starts a new timer session for a user.
     */
    @Transactional
    fun startTimerSession(
        userId: UUID,
        type: TimerSessionType,
        duration: Int? = null,
        startedAt: LocalDateTime? = null,
        completed: Boolean = false,
        completedAt: LocalDateTime? = null
    ): TimerSessionDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }

        val session = timerService.createTimerSession(
            user = user,
            type = type,
            durationOverride = duration,
            startedAt = startedAt,
            completed = completed,
            completedAt = completedAt
        )

        if (session.completed) {
            handleWorkSessionCompletion(session)
        }

        return TimerSessionDto.fromEntity(session)
    }
    
    /**
     * Completes a timer session and processes rewards.
     */
    @Transactional
    fun completeTimerSession(sessionId: UUID): TimerSessionDto {
        val session = timerService.completeTimerSession(sessionId)
        handleWorkSessionCompletion(session)
        return TimerSessionDto.fromEntity(session)
    }

    @Transactional
    fun updateTimerSession(
        sessionId: UUID,
        duration: Int? = null,
        completed: Boolean? = null,
        completedAt: LocalDateTime? = null,
        startedAt: LocalDateTime? = null
    ): TimerSessionDto {
        val existing = timerService.findById(sessionId)
        val wasCompleted = existing.completed

        val session = timerService.updateTimerSession(
            sessionId = sessionId,
            duration = duration,
            completed = completed,
            completedAt = completedAt,
            startedAt = startedAt
        )

        if (!wasCompleted && session.completed) {
            handleWorkSessionCompletion(session)
        }

        return TimerSessionDto.fromEntity(session)
    }
    
    /**
     * Gets the current timer session for a user if one is active.
     */
    fun getCurrentSession(userId: UUID): TimerSessionDto? {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        // Find the most recent session that is still active
        val activeSessions = timerService.getTimerSessionsByDateRange(
            userId,
            LocalDate.now().atStartOfDay(),
            LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1)
        ).filter { it.isActive() }
        
        return activeSessions.maxByOrNull { it.startedAt }?.let { TimerSessionDto.fromEntity(it) }
    }
    
    /**
     * Gets a user's timer settings.
     */
    fun getTimerSettings(userId: UUID): TimerSettingsDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val settings = timerService.getOrCreateTimerSettings(user)
        return TimerSettingsDto.fromEntity(settings)
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
    ): TimerSettingsDto {
        val settings = timerService.updateTimerSettings(
            userId = userId,
            workTime = workTime,
            shortBreakTime = shortBreakTime,
            longBreakTime = longBreakTime,
            longBreakInterval = longBreakInterval,
            autoStartBreaks = autoStartBreaks,
            autoStartPomodoros = autoStartPomodoros,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            notificationsEnabled = notificationsEnabled
        )
        
        return TimerSettingsDto.fromEntity(settings)
    }
    
    private fun handleWorkSessionCompletion(session: com.hobos.tamadoro.domain.timer.TimerSession) {
        if (session.type != TimerSessionType.WORK) return

        val actualDuration = session.calculateActualDuration()
        tamaService.rewardTamaForPomodoro(session.user.id, actualDuration)
    }
}

/**
 * DTO for timer session data.
 */
data class TimerSessionDto(
    val id: UUID,
    val userId: UUID,
    val type: String,
    val duration: Int,
    val completed: Boolean,
    val startedAt: String,
    val completedAt: String?
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.timer.TimerSession): TimerSessionDto {
            return TimerSessionDto(
                id = entity.id,
                userId = entity.user.id,
                type = when (entity.type) {
                    com.hobos.tamadoro.domain.timer.TimerSessionType.WORK -> "focus"
                    com.hobos.tamadoro.domain.timer.TimerSessionType.SHORT_BREAK -> "shortBreak"
                    com.hobos.tamadoro.domain.timer.TimerSessionType.LONG_BREAK -> "longBreak"
                },
                duration = entity.duration,
                completed = entity.completed,
                startedAt = entity.startedAt.toString(),
                completedAt = entity.completedAt?.toString()
            )
        }
    }
}

/**
 * DTO for timer settings data.
 */
data class TimerSettingsDto(
    val workTime: Int,
    val shortBreakTime: Int,
    val longBreakTime: Int,
    val longBreakInterval: Int,
    val autoStartBreaks: Boolean,
    val autoStartPomodoros: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val notificationsEnabled: Boolean
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.timer.TimerSettings): TimerSettingsDto {
            return TimerSettingsDto(
                workTime = entity.workTime,
                shortBreakTime = entity.shortBreakTime,
                longBreakTime = entity.longBreakTime,
                longBreakInterval = entity.longBreakInterval,
                autoStartBreaks = entity.autoStartBreaks,
                autoStartPomodoros = entity.autoStartPomodoros,
                soundEnabled = entity.soundEnabled,
                vibrationEnabled = entity.vibrationEnabled,
                notificationsEnabled = entity.notificationsEnabled
            )
        }
    }
}

/**
 * DTO for daily statistics data.
 */
