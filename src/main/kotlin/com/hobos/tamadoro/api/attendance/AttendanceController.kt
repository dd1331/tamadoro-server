package com.hobos.tamadoro.api.attendance

import com.hobos.tamadoro.application.attendance.AttendanceApplicationService
import com.hobos.tamadoro.application.attendance.AttendanceDto
import com.hobos.tamadoro.application.attendance.AttendanceStreakDto
import org.springframework.http.ResponseEntity
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for attendance-related endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
class AttendanceController(
    private val attendanceApplicationService: AttendanceApplicationService
) {
    /**
     * Gets attendance records for a user.
     */
    @GetMapping
    fun getAttendance(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<AttendanceDto>>> {
        val attendance = attendanceApplicationService.getAttendance(userId)
        return ResponseEntity.ok(ApiResponse.success(attendance))
    }
    
    /**
     * Checks in for attendance.
     */
    @PostMapping("/check")
    fun checkAttendance(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<AttendanceDto>> {
        val attendance = attendanceApplicationService.checkAttendance(userId)
        return ResponseEntity.ok(ApiResponse.success(attendance))
    }
    
    /**
     * Gets attendance streak for a user.
     */
    @GetMapping("/streak")
    fun getAttendanceStreak(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<AttendanceStreakDto>> {
        val streak = attendanceApplicationService.getAttendanceStreak(userId)
        return ResponseEntity.ok(ApiResponse.success(streak))
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