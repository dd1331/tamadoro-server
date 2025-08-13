package com.hobos.tamadoro.domain.stats

import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.task.TaskRepository
import com.hobos.tamadoro.domain.timer.TimerSessionRepository
import com.hobos.tamadoro.domain.timer.TimerSessionType
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

/**
 * Domain service for statistics-related business logic.
 */
@Service
class StatsService(
    private val dailyStatsRepository: DailyStatsRepository,
    private val timerSessionRepository: TimerSessionRepository,
    private val taskRepository: TaskRepository,
    private val userInventoryRepository: UserInventoryRepository,
    private val userRepository: UserRepository
) {
    /**
     * Gets or creates daily stats for a user on a specific date.
     */
    @Transactional
    fun getOrCreateDailyStats(user: User, date: LocalDate = LocalDate.now()): DailyStats {
        return dailyStatsRepository.findByUserIdAndDate(user.id, date)
            .orElseGet {
                val newStats = DailyStats(
                    user = user,
                    date = date
                )
                dailyStatsRepository.save(newStats)
            }
    }
    
    /**
     * Records attendance for a user on the current day.
     */
    @Transactional
    fun recordAttendance(userId: UUID): DailyStats {
        val today = LocalDate.now()
        val dailyStats = dailyStatsRepository.findByUserIdAndDate(userId, today)
            .orElseThrow { NoSuchElementException("Daily stats not found for user ID: $userId on date: $today") }
        
        if (!dailyStats.attendance) {
            dailyStats.markAttendance()
            
            // Calculate streak
            val streak = calculateAttendanceStreak(userId, today)
            
            // Reward based on streak
            rewardForAttendanceStreak(userId, streak)
        }
        
        return dailyStatsRepository.save(dailyStats)
    }
    
    /**
     * Gets daily stats for a user on a specific date.
     */
    fun getDailyStats(userId: UUID, date: LocalDate): DailyStats {
        return dailyStatsRepository.findByUserIdAndDate(userId, date)
            .orElseThrow { NoSuchElementException("Daily stats not found for user ID: $userId on date: $date") }
    }
    
    /**
     * Gets daily stats for a user within a date range.
     */
    fun getDailyStatsInRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<DailyStats> {
        return dailyStatsRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate)
    }
    
    /**
     * Gets weekly stats for a user for a specific week.
     */
    fun getWeeklyStats(userId: UUID, weekDate: LocalDate): WeeklyStats {
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = weekDate.get(weekFields.weekOfWeekBasedYear())
        val year = weekDate.get(weekFields.weekBasedYear())
        
        // Calculate the start and end of the week
        val firstDayOfWeek = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastDayOfWeek = firstDayOfWeek.plusDays(6)
        
        // Get daily stats for the week
        val dailyStats = getDailyStatsInRange(userId, firstDayOfWeek, lastDayOfWeek)
        
        // Calculate weekly totals
        val totalPomodoros = dailyStats.sumOf { it.completedPomodoros }
        val totalFocusTime = dailyStats.sumOf { it.totalFocusTime }
        val totalTasks = dailyStats.sumOf { it.completedTasks }
        val averageDailyFocus = if (dailyStats.isNotEmpty()) totalFocusTime / dailyStats.size else 0
        val streakDays = calculateAttendanceStreak(userId, lastDayOfWeek)
        val coinsEarned = dailyStats.sumOf { it.coinsEarned }
        val gemsEarned = dailyStats.sumOf { it.gemsEarned }
        
        return WeeklyStats(
            weekStart = firstDayOfWeek,
            totalPomodoros = totalPomodoros,
            totalFocusTime = totalFocusTime,
            totalTasks = totalTasks,
            averageDailyFocus = averageDailyFocus,
            streakDays = streakDays,
            coinsEarned = coinsEarned,
            gemsEarned = gemsEarned
        )
    }
    
    /**
     * Gets monthly stats for a user for a specific month.
     */
    fun getMonthlyStats(userId: UUID, month: YearMonth): MonthlyStats {
        // Calculate the start and end of the month
        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atEndOfMonth()
        
        // Get daily stats for the month
        val dailyStats = getDailyStatsInRange(userId, firstDayOfMonth, lastDayOfMonth)
        
        // Calculate monthly totals
        val totalPomodoros = dailyStats.sumOf { it.completedPomodoros }
        val totalFocusTime = dailyStats.sumOf { it.totalFocusTime }
        val totalTasks = dailyStats.sumOf { it.completedTasks }
        val averageDailyFocus = if (dailyStats.isNotEmpty()) totalFocusTime / dailyStats.size else 0
        
        // Find the best day by focus time
        val bestDay = dailyStats.maxByOrNull { it.totalFocusTime }
        
        return MonthlyStats(
            month = month.toString(),
            totalPomodoros = totalPomodoros,
            totalFocusTime = totalFocusTime,
            totalTasks = totalTasks,
            averageDailyFocus = averageDailyFocus,
            bestDay = bestDay?.let { BestDay(it.date.toString(), it.totalFocusTime) }
        )
    }
    
    /**
     * Calculates the attendance streak for a user up to a specific date.
     */
    fun calculateAttendanceStreak(userId: UUID, endDate: LocalDate): Int {
        return dailyStatsRepository.countConsecutiveAttendanceDays(userId, endDate).toInt()
    }

    /**
     * Returns a map of date -> completed pomodoros within the given range.
     */
    fun getPomodoroCountsByDateRange(userId: UUID, startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int> {
        val list = dailyStatsRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, startDate, endDate)
        return list.associate { it.date to it.completedPomodoros }
    }
    
    /**
     * Rewards a user based on their attendance streak.
     */
    @Transactional
    fun rewardForAttendanceStreak(userId: UUID, streak: Int) {
        val userInventory = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("User inventory not found for user ID: $userId") }
        
        // Base reward: 10 coins per day
        var coinsToAdd = 10
        
        // Bonus rewards for milestone streaks
        when {
            streak >= 365 -> {
                coinsToAdd = 100
                userInventory.addGems(10) // Big milestone: 1 year streak
            }
            streak >= 180 -> {
                coinsToAdd = 50
                userInventory.addGems(5) // 6 months streak
            }
            streak >= 90 -> {
                coinsToAdd = 30
                userInventory.addGems(3) // 3 months streak
            }
            streak >= 30 -> {
                coinsToAdd = 20
                userInventory.addGems(2) // 1 month streak
            }
            streak >= 7 -> {
                coinsToAdd = 15
                userInventory.addGems(1) // 1 week streak
            }
        }
        
        userInventory.addCoins(coinsToAdd)
        userInventoryRepository.save(userInventory)
    }
    
    /**
     * Updates daily stats based on completed timer sessions and tasks.
     */
    @Transactional
    fun updateDailyStats(userId: UUID, date: LocalDate = LocalDate.now()) {
        val user = userRepository.findById(userId).orElseThrow()
        val dailyStats = getOrCreateDailyStats(user = user, date)

        // Calculate completed pomodoros
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1)

        val completedWorkSessions = timerSessionRepository.findByUserIdAndStartedAtBetween(userId, startOfDay, endOfDay)
            .filter { it.completed && it.type == TimerSessionType.WORK }

        dailyStats.completedPomodoros = completedWorkSessions.size

        // Calculate total focus time
        dailyStats.totalFocusTime = completedWorkSessions.sumOf { it.calculateActualDuration() }

        // Calculate completed tasks
        val completedTasks = taskRepository.findByUserIdAndCompletedIsTrueAndCompletedAtBetween(userId, startOfDay, endOfDay)
        dailyStats.completedTasks = completedTasks.size

        dailyStatsRepository.save(dailyStats)
    }
}

/**
 * Data class representing weekly statistics.
 */
data class WeeklyStats(
    val weekStart: LocalDate,
    val totalPomodoros: Int,
    val totalFocusTime: Int,
    val totalTasks: Int,
    val averageDailyFocus: Int,
    val streakDays: Int,
    val coinsEarned: Int,
    val gemsEarned: Int
)

/**
 * Data class representing monthly statistics.
 */
data class MonthlyStats(
    val month: String,
    val totalPomodoros: Int,
    val totalFocusTime: Int,
    val totalTasks: Int,
    val averageDailyFocus: Int,
    val bestDay: BestDay?
)

/**
 * Data class representing the best day in terms of focus time.
 */
data class BestDay(
    val date: String,
    val focusTime: Int
)