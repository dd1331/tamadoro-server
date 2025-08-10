package com.hobos.tamadoro.application.task

import com.hobos.tamadoro.domain.stats.StatsService
import com.hobos.tamadoro.domain.task.Task
import com.hobos.tamadoro.domain.task.TaskPriority
import com.hobos.tamadoro.domain.task.TaskRepository
import com.hobos.tamadoro.domain.task.TaskService
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for task-related use cases.
 */
@Service
class TaskApplicationService(
    private val taskService: TaskService,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val statsService: StatsService
) {
    /**
     * Gets all tasks for a user.
     */
    fun getTasks(userId: UUID): List<TaskDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val tasks = taskRepository.findByUserId(userId)
        return tasks.map { TaskDto.fromEntity(it) }
    }
    
    /**
     * Gets a specific task by ID.
     */
    fun getTask(userId: UUID, taskId: UUID): TaskDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found with ID: $taskId") }
        
        // Ensure the task belongs to the user
        if (task.user.id != userId) {
            throw IllegalArgumentException("Task does not belong to the user")
        }
        
        return TaskDto.fromEntity(task)
    }
    
    /**
     * Creates a new task.
     */
    @Transactional
    fun createTask(userId: UUID, request: CreateTaskRequest): TaskDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val task = taskService.createTask(
            user = user,
            title = request.title,
            description = request.description,
            priority = request.priority,
            estimatedPomodoros = request.estimatedPomodoros
        )
        
        return TaskDto.fromEntity(task)
    }
    
    /**
     * Updates a task.
     */
    @Transactional
    fun updateTask(userId: UUID, taskId: UUID, request: UpdateTaskRequest): TaskDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found with ID: $taskId") }
        
        // Ensure the task belongs to the user
        if (task.user.id != userId) {
            throw IllegalArgumentException("Task does not belong to the user")
        }
        
        val updatedTask = taskService.updateTask(
            task = task,
            title = request.title,
            description = request.description,
            priority = request.priority,
            estimatedPomodoros = request.estimatedPomodoros
        )
        
        return TaskDto.fromEntity(updatedTask)
    }
    
    /**
     * Deletes a task.
     */
    @Transactional
    fun deleteTask(userId: UUID, taskId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found with ID: $taskId") }
        
        // Ensure the task belongs to the user
        if (task.user.id != userId) {
            throw IllegalArgumentException("Task does not belong to the user")
        }
        
        taskRepository.delete(task)
    }
    
    /**
     * Completes a task.
     */
    @Transactional
    fun completeTask(userId: UUID, taskId: UUID): TaskDto {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val task = taskRepository.findById(taskId)
            .orElseThrow { NoSuchElementException("Task not found with ID: $taskId") }
        
        // Ensure the task belongs to the user
        if (task.user.id != userId) {
            throw IllegalArgumentException("Task does not belong to the user")
        }
        
        val completedTask = taskService.completeTask(task)
        
        // Update daily stats
        statsService.updateDailyStats(userId)
        
        return TaskDto.fromEntity(completedTask)
    }
}

/**
 * DTO for task data.
 */
data class TaskDto(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val priority: String,
    val estimatedPomodoros: Int,
    val completedPomodoros: Int,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val progress: Int
) {
    companion object {
        fun fromEntity(entity: Task): TaskDto {
            return TaskDto(
                id = entity.id,
                userId = entity.user.id,
                title = entity.title,
                description = entity.description,
                completed = entity.completed,
                priority = entity.priority.name,
                estimatedPomodoros = entity.estimatedPomodoros,
                completedPomodoros = entity.completedPomodoros,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString(),
                completedAt = entity.completedAt?.toString(),
                progress = entity.calculateProgress()
            )
        }
    }
}

/**
 * Request for creating a task.
 */
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val priority: String = "MEDIUM",
    val estimatedPomodoros: Int = 1
)

/**
 * Request for updating a task.
 */
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val estimatedPomodoros: Int? = null,
    val completed: Boolean? = null,
    val completedPomodoros: Int? = null
) 