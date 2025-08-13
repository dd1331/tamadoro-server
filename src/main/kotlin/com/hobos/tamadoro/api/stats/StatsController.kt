package com.hobos.tamadoro.api.stats

import com.hobos.tamadoro.application.stats.StatsApplicationService
import com.hobos.tamadoro.application.stats.DailyStatsDto
import com.hobos.tamadoro.application.stats.WeeklyStatsDto
import com.hobos.tamadoro.application.stats.MonthlyStatsDto
import com.hobos.tamadoro.application.stats.WeeklyGoalDto
import com.hobos.tamadoro.application.stats.UpdateWeeklyGoalRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

/**
 * REST controller for statistics-related endpoints.
 */
@RestController
@RequestMapping("/api/stats")
class StatsController(
    private val statsApplicationService: StatsApplicationService
) {
    /**
     * Gets daily statistics for a user.
     */
    @GetMapping("/daily")
    fun getDailyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<ApiResponse<DailyStatsDto>> {
        val stats = statsApplicationService.getDailyStats(userId, date ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }
    
    /**
     * Gets daily statistics for a user within a date range.
     */
    @GetMapping("/daily/range")
    fun getDailyStatsRange(
        @CurrentUserId userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) start: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) end: LocalDate
    ): ResponseEntity<ApiResponse<List<DailyStatsDto>>> {
        val stats = statsApplicationService.getDailyStatsRange(userId, start, end)
        return ResponseEntity.ok(ApiResponse.success(stats))
    }
    
    /**
     * Gets weekly statistics for a user.
     */
    @GetMapping("/weekly")
    fun getWeeklyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) week: LocalDate?
    ): ResponseEntity<ApiResponse<WeeklyStatsDto>> {
        val stats = statsApplicationService.getWeeklyStats(userId, week ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }
    
    /**
     * Gets monthly statistics for a user.
     */
    @GetMapping("/monthly")
    fun getMonthlyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) month: String?
    ): ResponseEntity<ApiResponse<MonthlyStatsDto>> {
        val stats = statsApplicationService.getMonthlyStats(userId, month)
        return ResponseEntity.ok(ApiResponse.success(stats))
    }
    
    /**
     * Gets weekly goals for a user.
     */
    @GetMapping("/goals/weekly")
    fun getWeeklyGoals(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<WeeklyGoalDto>> {
        val goals = statsApplicationService.getWeeklyGoals(userId)
        return ResponseEntity.ok(ApiResponse.success(goals))
    }
    
    /**
     * Updates weekly goals for a user.
     */
    @PutMapping("/goals/weekly")
    fun updateWeeklyGoals(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateWeeklyGoalRequest
    ): ResponseEntity<ApiResponse<WeeklyGoalDto>> {
        val goals = statsApplicationService.updateWeeklyGoals(userId, request)
        return ResponseEntity.ok(ApiResponse.success(goals))
    }

    @PostMapping("/pomodoros")
    fun postPomodoro(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        statsApplicationService.recordPomodoroEvent(userId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @GetMapping("/pomodoros/heatmap")
    fun getPomodoroHeatmap(
        @CurrentUserId userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) start: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) end: LocalDate
    ): ResponseEntity<ApiResponse<Unit>> {
        return TODO("Provide the return value")
    }
    @PostMapping("/tasks")
    fun postTaskEvent(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        statsApplicationService.recordTaskEvent(userId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}

/**
 * Generic API response wrapper.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> error(code: Int, message: String, details: Any? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorResponse(code, message, details)
            )
        }
    }
}

/**
 * Error response.
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
    val details: Any? = null
) 