package com.hobos.tamadoro.application.timer

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.stats.StatsService
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.timer.TimerService
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.domain.user.User
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
    private val statsService: StatsService,
    private val userRepository: UserRepository,
    private val userInventoryRepository: UserInventoryRepository
) {
    /**
     * Starts a new timer session for a user.
     */
    @Transactional
    fun startTimerSession(
        userId: UUID,
        type: TimerSessionType,
        taskId: UUID? = null,
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
            taskId = taskId,
            durationOverride = duration,
            startedAt = startedAt,
            completed = completed,
            completedAt = completedAt
        )

        // Mark attendance for the day if this is the first session
        if (!statsService.getDailyStats(userId, LocalDate.now()).attendance) {
            statsService.recordAttendance(userId)
        }

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
        startedAt: LocalDateTime? = null,
        taskId: UUID? = null
    ): TimerSessionDto {
        val existing = timerService.findById(sessionId)
        val wasCompleted = existing.completed

        val session = timerService.updateTimerSession(
            sessionId = sessionId,
            duration = duration,
            completed = completed,
            completedAt = completedAt,
            startedAt = startedAt,
            taskId = taskId
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
    
    /**
     * Gets daily statistics for a user.
     */
    fun getDailyStatistics(userId: UUID, date: LocalDate = LocalDate.now()): DailyStatisticsDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val dailyStats = statsService.getDailyStats(userId, date)
        val completedSessions = timerService.getCompletedWorkSessionsByDate(userId, date)
        val focusTime = timerService.calculateTotalFocusTimeForDate(userId, date)
        
        return DailyStatisticsDto(
            date = date.toString(),
            completedPomodoros = dailyStats.completedPomodoros,
            totalFocusTime = focusTime,
            completedTasks = dailyStats.completedTasks,
            attendance = dailyStats.attendance,
            coinsEarned = dailyStats.coinsEarned,
            gemsEarned = dailyStats.gemsEarned,
            productivityScore = dailyStats.calculateProductivityScore()
        )
    }

    private fun handleWorkSessionCompletion(session: com.hobos.tamadoro.domain.timer.TimerSession) {
        if (session.type != TimerSessionType.WORK) return

        val actualDuration = session.calculateActualDuration()
        tamaService.rewardTamaForPomodoro(session.user.id, actualDuration)

        val userInventory = userInventoryRepository.findByUserId(session.user.id)
        userInventory.ifPresent {
            it.addCoins(actualDuration)
            userInventoryRepository.save(it)
        }

        statsService.updateDailyStats(session.user.id)
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
    val completedAt: String?,
    val taskId: UUID?
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
                completedAt = entity.completedAt?.toString(),
                taskId = entity.taskId
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
data class DailyStatisticsDto(
    val date: String,
    val completedPomodoros: Int,
    val totalFocusTime: Int,
    val completedTasks: Int,
    val attendance: Boolean,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val productivityScore: Int
)
