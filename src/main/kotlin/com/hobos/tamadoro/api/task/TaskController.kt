package com.hobos.tamadoro.api.task

import com.hobos.tamadoro.application.task.TaskApplicationService
import com.hobos.tamadoro.application.task.TaskDto
import com.hobos.tamadoro.application.task.CreateTaskRequest
import com.hobos.tamadoro.application.task.UpdateTaskRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for task-related endpoints.
 */
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskApplicationService: TaskApplicationService
) {
    /**
     * Gets all tasks for a user.
     */
    @GetMapping
    fun getTasks(@RequestHeader("User-ID") userId: UUID): ResponseEntity<ApiResponse<List<TaskDto>>> {
        val tasks = taskApplicationService.getTasks(userId)
        return ResponseEntity.ok(ApiResponse.success(tasks))
    }
    
    /**
     * Gets a specific task by ID.
     */
    @GetMapping("/{taskId}")
    fun getTask(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.getTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success(task))
    }
    
    /**
     * Creates a new task.
     */
    @PostMapping
    fun createTask(
        @RequestHeader("User-ID") userId: UUID,
        @RequestBody request: CreateTaskRequest
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.createTask(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(task))
    }
    
    /**
     * Updates a task.
     */
    @PutMapping("/{taskId}")
    fun updateTask(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.updateTask(userId, taskId, request)
        return ResponseEntity.ok(ApiResponse.success(task))
    }
    
    /**
     * Deletes a task.
     */
    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        taskApplicationService.deleteTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
    
    /**
     * Completes a task.
     */
    @PostMapping("/{taskId}/complete")
    fun completeTask(
        @RequestHeader("User-ID") userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.completeTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success(task))
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