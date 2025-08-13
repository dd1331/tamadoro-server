package com.hobos.tamadoro.application.stats

import com.hobos.tamadoro.domain.stats.StatsService
import com.hobos.tamadoro.domain.stats.DailyStats
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import java.util.NoSuchElementException

/**
 * Application service for statistics-related use cases.
 */
@Service
class StatsApplicationService(
    private val statsService: StatsService,
    private val userRepository: UserRepository
) {
    /**
     * Gets daily statistics for a user.
     */
    fun getDailyStats(userId: UUID, date: LocalDate): DailyStatsDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val dailyStats = statsService.getDailyStats(userId, date)
        return DailyStatsDto.fromEntity(dailyStats)
    }
    
    /**
     * Gets daily statistics for a user within a date range.
     */
    fun getDailyStatsRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<DailyStatsDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val dailyStats = statsService.getDailyStatsInRange(userId, startDate, endDate)
        return dailyStats.map { DailyStatsDto.fromEntity(it) }
    }
    
    /**
     * Gets weekly statistics for a user.
     */
    fun getWeeklyStats(userId: UUID, weekDate: LocalDate): WeeklyStatsDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val weeklyStats = statsService.getWeeklyStats(userId, weekDate)
        return WeeklyStatsDto.fromEntity(weeklyStats)
    }
    
    /**
     * Gets monthly statistics for a user.
     */
    fun getMonthlyStats(userId: UUID, monthString: String?): MonthlyStatsDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val month = monthString?.let { YearMonth.parse(it) } ?: YearMonth.now()
        val monthlyStats = statsService.getMonthlyStats(userId, month)
        return MonthlyStatsDto.fromEntity(monthlyStats)
    }
    
    /**
     * Gets weekly goals for a user.
     */
    fun getWeeklyGoals(userId: UUID): WeeklyGoalDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        // For now, return default goals. In a real implementation, this would be stored in the database.
        return WeeklyGoalDto(
            pomodoros = 20,
            focusTime = 480, // 8 hours in minutes
            tasks = 10
        )
    }
    
    /**
     * Updates weekly goals for a user.
     */
    fun updateWeeklyGoals(userId: UUID, request: UpdateWeeklyGoalRequest): WeeklyGoalDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        // For now, just return the updated goals. In a real implementation, this would be stored in the database.
        return WeeklyGoalDto(
            pomodoros = request.pomodoros,
            focusTime = request.focusTime,
            tasks = request.tasks
        )
    }

    fun recordPomodoroEvent(userId: UUID) {
        statsService.updateDailyStats(userId)
    }

    fun recordTaskEvent(userId: UUID) {
        statsService.updateDailyStats(userId)
    }
    fun getPomodoroHeatmap( userId: UUID, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int> {
        return statsService.getPomodoroCountsByDateRange(userId, startDate, endDate)
    }
}

/**
 * DTO for daily statistics data.
 */
data class DailyStatsDto(
    val date: String,
    val completedPomodoros: Int,
    val totalFocusTime: Int,
    val completedTasks: Int,
    val attendance: Boolean,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val productivityScore: Int
) {
    companion object {
        fun fromEntity(entity: DailyStats): DailyStatsDto {
            return DailyStatsDto(
                date = entity.date.toString(),
                completedPomodoros = entity.completedPomodoros,
                totalFocusTime = entity.totalFocusTime,
                completedTasks = entity.completedTasks,
                attendance = entity.attendance,
                coinsEarned = entity.coinsEarned,
                gemsEarned = entity.gemsEarned,
                productivityScore = entity.calculateProductivityScore()
            )
        }
    }
}

/**
 * DTO for weekly statistics data.
 */
data class WeeklyStatsDto(
    val weekStart: String,
    val totalPomodoros: Int,
    val totalFocusTime: Int,
    val totalTasks: Int,
    val averageDailyFocus: Int,
    val streakDays: Int,
    val coinsEarned: Int,
    val gemsEarned: Int
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.stats.WeeklyStats): WeeklyStatsDto {
            return WeeklyStatsDto(
                weekStart = entity.weekStart.toString(),
                totalPomodoros = entity.totalPomodoros,
                totalFocusTime = entity.totalFocusTime,
                totalTasks = entity.totalTasks,
                averageDailyFocus = entity.averageDailyFocus,
                streakDays = entity.streakDays,
                coinsEarned = entity.coinsEarned,
                gemsEarned = entity.gemsEarned
            )
        }
    }
}

/**
 * DTO for monthly statistics data.
 */
data class MonthlyStatsDto(
    val month: String,
    val totalPomodoros: Int,
    val totalFocusTime: Int,
    val totalTasks: Int,
    val averageDailyFocus: Int,
    val bestDay: BestDayDto?
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.stats.MonthlyStats): MonthlyStatsDto {
            return MonthlyStatsDto(
                month = entity.month,
                totalPomodoros = entity.totalPomodoros,
                totalFocusTime = entity.totalFocusTime,
                totalTasks = entity.totalTasks,
                averageDailyFocus = entity.averageDailyFocus,
                bestDay = entity.bestDay?.let { BestDayDto.fromEntity(it) }
            )
        }
    }
}

/**
 * DTO for best day data.
 */
data class BestDayDto(
    val date: String,
    val focusTime: Int
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.stats.BestDay): BestDayDto {
            return BestDayDto(
                date = entity.date,
                focusTime = entity.focusTime
            )
        }
    }
}

/**
 * DTO for weekly goals data.
 */
data class WeeklyGoalDto(
    val pomodoros: Int,
    val focusTime: Int,
    val tasks: Int
)

/**
 * Request for updating weekly goals.
 */
data class UpdateWeeklyGoalRequest(
    val pomodoros: Int,
    val focusTime: Int,
    val tasks: Int
) 

/**
 * Heatmap DTO for date->count.
 */
data class HeatmapPointDto(
    val date: String,
    val count: Int
)

fun LocalDate.datesUntilInclusive(end: LocalDate): Sequence<LocalDate> =
    generateSequence(this) { current ->
        val next = current.plusDays(1)
        if (next.isAfter(end)) null else next
    }

fun LocalDate.rangeInclusive(end: LocalDate): List<LocalDate> =
    listOf(this) + this.datesUntilInclusive(end).toList()

/**
 * Builds a dense heatmap list for start...end, filling missing days with 0.
 */
fun buildHeatmap(start: LocalDate, end: LocalDate, counts: Map<LocalDate, Int>): List<HeatmapPointDto> {
    val days = mutableListOf<LocalDate>()
    var d = start
    while (!d.isAfter(end)) {
        days.add(d)
        d = d.plusDays(1)
    }
    return days.map { day -> HeatmapPointDto(date = day.toString(), count = counts[day] ?: 0) }
}

