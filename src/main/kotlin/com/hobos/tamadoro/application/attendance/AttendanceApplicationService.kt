package com.hobos.tamadoro.application.attendance

import com.hobos.tamadoro.domain.stats.StatsService
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Application service for attendance-related use cases.
 */
@Service
class AttendanceApplicationService(
    private val statsService: StatsService,
    private val userRepository: UserRepository
) {
    /**
     * Gets attendance records for a user.
     */
    fun getAttendance(userId: UUID): List<AttendanceDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        // Get the last 30 days of attendance
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(30)
        
        val dailyStats = statsService.getDailyStatsInRange(userId, startDate, endDate)
        return dailyStats.filter { it.attendance }.map { AttendanceDto.fromEntity(it) }
    }
    
    /**
     * Checks in for attendance.
     */
    @Transactional
    fun checkAttendance(userId: UUID): AttendanceDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val dailyStats = statsService.recordAttendance(userId)
        return AttendanceDto.fromEntity(dailyStats)
    }
    
    /**
     * Gets attendance streak for a user.
     */
    fun getAttendanceStreak(userId: UUID): AttendanceStreakDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val streak = statsService.calculateAttendanceStreak(userId, LocalDate.now())
        return AttendanceStreakDto(streak = streak)
    }
}

/**
 * DTO for attendance data.
 */
data class AttendanceDto(
    val date: String,
    val checkedAt: String,
    val streakDays: Int,
    val reward: AttendanceRewardDto
) {
    companion object {
        fun fromEntity(entity: com.hobos.tamadoro.domain.stats.DailyStats): AttendanceDto {
            // Calculate streak for this date
            val streak = entity.date.dayOfWeek.value // Simplified calculation
            val reward = AttendanceRewardDto(
                coins = entity.coinsEarned,
                gems = entity.gemsEarned
            )
            
            return AttendanceDto(
                date = entity.date.toString(),
                checkedAt = entity.date.atStartOfDay().toString(), // Simplified
                streakDays = streak,
                reward = reward
            )
        }
    }
}

/**
 * DTO for attendance reward data.
 */
data class AttendanceRewardDto(
    val coins: Int,
    val gems: Int
)

/**
 * DTO for attendance streak data.
 */
data class AttendanceStreakDto(
    val streak: Int
) 