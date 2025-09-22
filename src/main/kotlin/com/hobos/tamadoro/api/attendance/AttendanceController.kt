package com.hobos.tamadoro.api.attendance

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.attendance.AttendanceApplicationService
import com.hobos.tamadoro.application.attendance.AttendanceDto
import com.hobos.tamadoro.application.attendance.AttendanceStreakDto
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for attendance-related endpoints.
 */
@RestController
@RequestMapping("/attendance")
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
