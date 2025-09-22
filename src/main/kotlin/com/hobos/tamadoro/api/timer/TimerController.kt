package com.hobos.tamadoro.api.timer

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.timer.DailyStatisticsDto
import com.hobos.tamadoro.application.timer.TimerApplicationService
import com.hobos.tamadoro.application.timer.TimerSessionDto
import com.hobos.tamadoro.application.timer.TimerSettingsDto
import com.hobos.tamadoro.config.CurrentUserId
import com.hobos.tamadoro.domain.timer.TimerSessionType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDate
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

    @PostMapping("/sessions")
    fun startTimerSession(
        @CurrentUserId userId: UUID,
        @Valid @RequestBody request: StartTimerSessionRequest
    ): ResponseEntity<ApiResponse<TimerSessionDto>> {
        val sessionType = request.type.toTimerSessionType()
        val session = timerApplicationService.startTimerSession(
            userId = userId,
            type = sessionType,
            taskId = request.taskId,
            duration = request.duration,
            startedAt = parseDateTime(request.startedAt),
            completed = request.completed ?: false,
            completedAt = parseDateTime(request.completedAt)
        )
        return ResponseEntity.created(URI.create("/timer/sessions/${session.id}")).body(ApiResponse.success(session))
    }

    @PutMapping("/sessions/{sessionId}")
    fun updateTimerSession(
        @PathVariable sessionId: UUID,
        @RequestBody body: TimerSessionUpdateRequest
    ): ResponseEntity<ApiResponse<TimerSessionDto>> {
        val session = timerApplicationService.updateTimerSession(
            sessionId = sessionId,
            duration = body.duration,
            completed = body.completed,
            completedAt = parseDateTime(body.completedAt),
            startedAt = parseDateTime(body.startedAt),
            taskId = body.taskId
        )
        return ResponseEntity.ok(ApiResponse.success(session))
    }

    @GetMapping("/sessions/current")
    fun getCurrentSession(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<TimerSessionDto?>> {
        val session = timerApplicationService.getCurrentSession(userId)
        return ResponseEntity.ok(ApiResponse.successNullable(session))
    }

    @GetMapping("/stats/daily")
    fun getDailyStatistics(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<ApiResponse<DailyStatisticsDto>> {
        val stats = timerApplicationService.getDailyStatistics(userId, date ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    private fun String.toTimerSessionType(): TimerSessionType {
        val normalized = replace("_", "").replace(" ", "").lowercase()
        return when (normalized) {
            "work", "focus" -> TimerSessionType.WORK
            "shortbreak", "short" -> TimerSessionType.SHORT_BREAK
            "longbreak", "long" -> TimerSessionType.LONG_BREAK
            else -> throw IllegalArgumentException("Invalid timer session type: $this")
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(value).toLocalDateTime()
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(value)
            } catch (ex: DateTimeParseException) {
                LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }
}

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
    @field:NotBlank
    val type: String,
    val duration: Int,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val completed: Boolean? = null,
    val taskId: UUID? = null
)

data class TimerSessionUpdateRequest(
    val duration: Int? = null,
    val completed: Boolean? = null,
    val completedAt: String? = null,
    val startedAt: String? = null,
    val taskId: UUID? = null
)
