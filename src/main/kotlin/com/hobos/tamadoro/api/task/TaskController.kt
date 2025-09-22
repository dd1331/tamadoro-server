package com.hobos.tamadoro.api.task

import com.hobos.tamadoro.api.common.ApiResponse
import com.hobos.tamadoro.application.task.CreateTaskRequest
import com.hobos.tamadoro.application.task.TaskApplicationService
import com.hobos.tamadoro.application.task.TaskDto
import com.hobos.tamadoro.application.task.UpdateTaskRequest
import com.hobos.tamadoro.config.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskApplicationService: TaskApplicationService
) {
    @GetMapping
    fun getTasks(@CurrentUserId userId: UUID): ResponseEntity<ApiResponse<List<TaskDto>>> {
        val tasks = taskApplicationService.getTasks(userId)
        return ResponseEntity.ok(ApiResponse.success(tasks))
    }

    @GetMapping("/{taskId}")
    fun getTask(
        @CurrentUserId userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.getTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success(task))
    }

    @PostMapping
    fun createTask(
        @CurrentUserId userId: UUID,
        @RequestBody request: CreateTaskRequest
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.createTask(userId, request)
        return ResponseEntity.created(URI.create("/tasks/${task.id}")).body(ApiResponse.success(task))
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        @CurrentUserId userId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<ApiResponse<TaskDto>> {
        val task = taskApplicationService.updateTask(userId, taskId, request)
        return ResponseEntity.ok(ApiResponse.success(task))
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @CurrentUserId userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        taskApplicationService.deleteTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success())
    }

    @PostMapping("/{taskId}/complete")
    fun completeTask(
        @CurrentUserId userId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        taskApplicationService.completeTask(userId, taskId)
        return ResponseEntity.ok(ApiResponse.success())
    }
}
