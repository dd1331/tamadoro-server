package com.hobos.tamadoro.application.timer

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.stats.StatsService
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.task.TaskRepository
import com.hobos.tamadoro.domain.timer.TimerService
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
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
    private val taskRepository: TaskRepository,
    private val userInventoryRepository: UserInventoryRepository
) {
    /**
     * Starts a new timer session for a user.
     */
    @Transactional
    fun startTimerSession(userId: UUID, type: TimerSessionType, taskId: UUID? = null): TimerSessionDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val session = timerService.createTimerSession(user, type, taskId)
        
        // Mark attendance for the day if this is the first session
        if (!statsService.getDailyStats(userId, LocalDate.now()).attendance) {
            statsService.recordAttendance(userId)
        }
        
        return TimerSessionDto.fromEntity(session)
    }
    
    /**
     * Completes a timer session and processes rewards.
     */
    @Transactional
    fun completeTimerSession(sessionId: UUID): TimerSessionDto {
        val session = timerService.completeTimerSession(sessionId)
        
        // If it's a work session, reward the user's active tama
        if (session.type == TimerSessionType.WORK) {
            val actualDuration = session.calculateActualDuration()
            tamaService.rewardTamaForPomodoro(session.user.id, actualDuration)
            
            // Update user's inventory with coins earned (1 coin per minute)
            val userInventory = userInventoryRepository.findByUserId(session.user.id)
            userInventory.ifPresent {
                it.addCoins(actualDuration)
                userInventoryRepository.save(it)
            }
            
            // Update statistics
            statsService.updateDailyStats(session.user.id)
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
        shortBreakTime: Int? = null,
        longBreakTime: Int? = null,
        longBreakInterval: Int? = null,
        autoStartBreaks: Boolean? = null,
        autoStartPomodoros: Boolean? = null,
        soundEnabled: Boolean? = null,
        vibrationEnabled: Boolean? = null
    ): TimerSettingsDto {
        val settings = timerService.updateTimerSettings(
            userId = userId,
            shortBreakTime = shortBreakTime,
            longBreakTime = longBreakTime,
            longBreakInterval = longBreakInterval,
            autoStartBreaks = autoStartBreaks,
            autoStartPomodoros = autoStartPomodoros,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled
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
    val taskId: UUID?,
    val remainingTime: Int
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.timer.TimerSession): TimerSessionDto {
            return TimerSessionDto(
                id = entity.id,
                userId = entity.user.id,
                type = entity.type.name,
                duration = entity.duration,
                completed = entity.completed,
                startedAt = entity.startedAt.toString(),
                completedAt = entity.completedAt?.toString(),
                taskId = entity.taskId,
                remainingTime = entity.remainingTime()
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