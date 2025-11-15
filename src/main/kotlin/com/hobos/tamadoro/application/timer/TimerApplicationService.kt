package com.hobos.tamadoro.application.timer

import com.hobos.tamadoro.domain.tamas.TamaService
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.timer.TimerService
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for timer-related use cases.
 * This service coordinates multiple domain services to implement higher-level operations.
 */
@Service
class TimerApplicationService(
    private val timerService: TimerService,
    private val tamaService: TamaService,
    private val userRepository: UserRepository,
    private val tamaRepository: UserTamaRepository
) {
    /**
     * Starts a new timer session for a user.
     */
    @Transactional
    fun completeSession(
        userId: UUID,
        type: TimerSessionType,
        time: Int

    ) {
        val activeTama = tamaRepository.findOneByUserIdAndIsActiveTrue(userId) ?: throw NoSuchElementException()


        val xp = getXpToAdd(type, time)

        activeTama.addExperience(xp)

        return
    }

    private fun getXpToAdd(type: TimerSessionType, time: Int): Int {
        if(type == TimerSessionType.CLASSIC) return time * 5

        return time * 10
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
    

}

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
