package com.hobos.tamadoro.api.timer

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.timer.TimerApplicationService
import com.hobos.tamadoro.application.timer.TimerSettingsDto
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.timer.TimerSessionType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

@RestController
@RequestMapping("/timer")
class TimerController(
    private val timerApplicationService: TimerApplicationService
) {
    @GetMapping("/settings")
    fun getTimerSettings(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<TimerSettingsDto>> {
        val settings = timerApplicationService.getTimerSettings(userId)
        return ResponseEntity.ok(ApiResponse.success(settings))
    }

    @PostMapping("/completed")
    fun completeTimerSession(@CurrentUserId userId: UUID, @RequestBody request: CompleteSessionRequest): ResponseEntity<ApiResponse<Unit>> {
        timerApplicationService.completeSession(
            userId = userId,
            type = request.type,
            time = request.completedTime
        )
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PutMapping("/settings")
    fun updateTimerSettings(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody request: UpdateTimerSettingsRequest
    ): ResponseEntity<ApiResponse<TimerSettingsDto>> {
        val settings = timerApplicationService.updateTimerSettings(
            userId = userId,
            workTime = request.workTime,
            shortBreakTime = request.shortBreakTime,
            longBreakTime = request.longBreakTime,
            longBreakInterval = request.longBreakInterval,
            autoStartBreaks = request.autoStartBreaks,
            autoStartPomodoros = request.autoStartPomodoros,
            soundEnabled = request.soundEnabled,
            vibrationEnabled = request.vibrationEnabled,
            notificationsEnabled = request.notificationsEnabled
        )
        return ResponseEntity.ok(ApiResponse.success(settings))
    }






}

data class CompleteSessionRequest(
    val type: TimerSessionType,
    val completedTime: Int
)


data class UpdateTimerSettingsRequest(
    val workTime: Int? = null,
    val shortBreakTime: Int? = null,
    val longBreakTime: Int? = null,
    val longBreakInterval: Int? = null,
    val autoStartBreaks: Boolean? = null,
    val autoStartPomodoros: Boolean? = null,
    val soundEnabled: Boolean? = null,
    val vibrationEnabled: Boolean? = null,
    val notificationsEnabled: Boolean? = null
)

data class StartTimerSessionRequest(
    val type: TimerSessionType,
    val duration: Int,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val completed: Boolean? = null
)

data class TimerSessionUpdateRequest(
    val duration: Int? = null,
    val completed: Boolean? = null,
    val completedAt: String? = null,
    val startedAt: String? = null
)
