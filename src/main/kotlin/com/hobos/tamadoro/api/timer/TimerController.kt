package com.hobos.tamadoro.api.timer

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.timer.DailyStatisticsDto
import com.hobos.tamadoro.application.timer.TimerApplicationService
import com.hobos.tamadoro.application.timer.TimerSessionDto
import com.hobos.tamadoro.application.timer.TimerSettingsDto
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.config.CurrentUserId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * REST controller for timer-related endpoints.
 */
@RestController
@RequestMapping("/api/timer")
class TimerController(
    private val timerApplicationService: TimerApplicationService
) {
    /**
     * Gets a user's timer settings.
     */
    @GetMapping("/settings")
    fun getTimerSettings(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<TimerSettingsDto>> {
        val settings = timerApplicationService.getTimerSettings(userId)
        return ResponseEntity.ok(ApiResponse.success(settings))
    }
    
    /**
     * Updates a user's timer settings.
     */
    @PutMapping("/settings")
    fun updateTimerSettings(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody request: UpdateTimerSettingsRequest
    ): ResponseEntity<ApiResponse<TimerSettingsDto>> {
        val settings = timerApplicationService.updateTimerSettings(
            userId = userId,
            shortBreakTime = request.shortBreakTime,
            longBreakTime = request.longBreakTime,
            longBreakInterval = request.longBreakInterval,
            autoStartBreaks = request.autoStartBreaks,
            autoStartPomodoros = request.autoStartPomodoros,
            soundEnabled = request.soundEnabled,
            vibrationEnabled = request.vibrationEnabled
        )
        return ResponseEntity.ok(ApiResponse.success(settings))
    }
    
    /**
     * Starts a new timer session.
     */
    @PostMapping("/sessions")
    fun startTimerSession(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody request: StartTimerSessionRequest
    ): ResponseEntity<ApiResponse<TimerSessionDto>> {
        val sessionType = when (request.type.lowercase()) {
            "work" -> TimerSessionType.WORK
            "shortbreak" -> TimerSessionType.SHORT_BREAK
            "longbreak" -> TimerSessionType.LONG_BREAK
            else -> throw IllegalArgumentException("Invalid timer session type: ${request.type}")
        }
        
        val session = timerApplicationService.startTimerSession(userId, sessionType, request.taskId)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(session))
    }
    
    /**
     * Completes a timer session.
     */
    @PutMapping("/sessions/{sessionId}")
    fun updateTimerSession(
        @PathVariable sessionId: UUID,
        @RequestBody body: TimerSessionUpdateRequest
    ): ResponseEntity<ApiResponse<TimerSessionDto>> {
        val session = timerApplicationService.completeTimerSession(sessionId)
        return ResponseEntity.ok(ApiResponse.success(session))
    }
    
    /**
     * Gets the current active timer session for a user.
     */
    @GetMapping("/sessions/current")
    fun getCurrentSession(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<TimerSessionDto?>> {
        val session = timerApplicationService.getCurrentSession(userId)
        return ResponseEntity.ok(ApiResponse.success(session))
    }
    
    /**
     * Gets daily statistics for a user.
     */
    @GetMapping("/stats/daily")
    fun getDailyStatistics(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<ApiResponse<DailyStatisticsDto>> {
        val stats = timerApplicationService.getDailyStatistics(userId, date ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }
}

/**
 * Request for updating timer settings.
 */
data class UpdateTimerSettingsRequest(
    val shortBreakTime: Int? = null,
    val longBreakTime: Int? = null,
    val longBreakInterval: Int? = null,
    val autoStartBreaks: Boolean? = null,
    val autoStartPomodoros: Boolean? = null,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null
)

/**
 * Request for starting a timer session.
 */
data class StartTimerSessionRequest(
    @field:NotBlank
    val type: String,
    val taskId: UUID? = null
)

data class TimerSessionUpdateRequest(
    val completed: Boolean? = null
)