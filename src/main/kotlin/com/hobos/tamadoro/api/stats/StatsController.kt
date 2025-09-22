package com.hobos.tamadoro.api.stats

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.stats.DailyStatsDto
import com.hobos.tamadoro.application.stats.MonthlyStatsDto
import com.hobos.tamadoro.application.stats.StatsApplicationService
import com.hobos.tamadoro.application.stats.UpdateWeeklyGoalRequest
import com.hobos.tamadoro.application.stats.WeeklyGoalDto
import com.hobos.tamadoro.application.stats.WeeklyStatsDto
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/stats")
class StatsController(
    private val statsApplicationService: StatsApplicationService
) {
    @GetMapping("/daily", params = ["startDate", "endDate"])
    fun getDailyStatsRange(
        @CurrentUserId userId: UUID,
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<DailyStatsDto>>> {
        val stats = statsApplicationService.getDailyStatsRange(userId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    @GetMapping("/daily")
    fun getDailyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<ApiResponse<DailyStatsDto>> {
        val stats = statsApplicationService.getDailyStats(userId, date ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    @GetMapping("/weekly")
    fun getWeeklyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) week: LocalDate?
    ): ResponseEntity<ApiResponse<WeeklyStatsDto>> {
        val stats = statsApplicationService.getWeeklyStats(userId, week ?: LocalDate.now())
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    @GetMapping("/monthly")
    fun getMonthlyStats(
        @CurrentUserId userId: UUID,
        @RequestParam(required = false) month: String?
    ): ResponseEntity<ApiResponse<MonthlyStatsDto>> {
        val stats = statsApplicationService.getMonthlyStats(userId, month)
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    @GetMapping("/goals/weekly")
    fun getWeeklyGoals(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<WeeklyGoalDto>> {
        val goals = statsApplicationService.getWeeklyGoals(userId)
        return ResponseEntity.ok(ApiResponse.success(goals))
    }

    @PutMapping("/goals/weekly")
    fun updateWeeklyGoals(
        @CurrentUserId userId: UUID,
        @RequestBody request: UpdateWeeklyGoalRequest
    ): ResponseEntity<ApiResponse<WeeklyGoalDto>> {
        val goals = statsApplicationService.updateWeeklyGoals(userId, request)
        return ResponseEntity.ok(ApiResponse.success(goals))
    }

    @PostMapping("/pomodoros")
    fun recordPomodoro(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        statsApplicationService.recordPomodoroEvent(userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/tasks")
    fun recordTask(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        statsApplicationService.recordTaskEvent(userId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @GetMapping("/pomodoros/heatmap")
    fun getPomodoroHeatmap(
        @CurrentUserId userId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) start: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) end: LocalDate
    ): ResponseEntity<ApiResponse<Map<LocalDate, Int>>> {
        val heatmap = statsApplicationService.getPomodoroHeatmap(userId, start, end)
        return ResponseEntity.ok(ApiResponse.success(heatmap))
    }
}
