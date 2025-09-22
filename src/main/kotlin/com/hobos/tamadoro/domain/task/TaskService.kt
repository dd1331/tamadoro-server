package com.hobos.tamadoro.domain.task

import com.hobos.tamadoro.domain.stats.DailyStatsRepository
import com.hobos.tamadoro.domain.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain service for task-related business logic.
 */
@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val dailyStatsRepository: DailyStatsRepository
) {
    /**
     * Creates a new task for a user.
     */
    @Transactional
    fun createTask(
        user: User,
        title: String,
        description: String?,
        priority: String,
        estimatedPomodoros: Int
    ): Task {
        val taskPriority = TaskPriority.valueOf(priority.uppercase())
        
        val task = Task(
            user = user,
            title = title,
            description = description,
            priority = taskPriority,
            estimatedPomodoros = estimatedPomodoros
        )
        
        return taskRepository.save(task)
    }
    
    /**
     * Updates a task.
     */
    @Transactional
    fun updateTask(
        task: Task,
        title: String?,
        description: String?,
        priority: String?,
        estimatedPomodoros: Int?,
        completed: Boolean?,
        completedPomodoros: Int?
    ): Task {
        task.update(
            title = title,
            description = description,
            priority = priority?.let { TaskPriority.valueOf(it.uppercase()) },
            estimatedPomodoros = estimatedPomodoros
        )
        completed?.let {
            if (it) task.complete() else task.reopen()
        }
        completedPomodoros?.let {
            task.completedPomodoros = it.coerceAtLeast(0)
        }

        return taskRepository.save(task)
    }
    
    /**
     * Completes a task.
     */
    @Transactional
    fun completeTask(task: Task): Task {
        task.complete()
        return taskRepository.save(task)
    }
    
    /**
     * Reopens a completed task.
     */
    @Transactional
    fun reopenTask(task: Task): Task {
        task.reopen()
        return taskRepository.save(task)
    }
    
    /**
     * Gets all tasks for a user.
     */
    fun getTasksForUser(userId: java.util.UUID): List<Task> {
        return taskRepository.findByUserId(userId)
    }
    
    /**
     * Gets all completed tasks for a user.
     */
    fun getCompletedTasksForUser(userId: java.util.UUID): List<Task> {
        return taskRepository.findByUserIdAndCompleted(userId, true)
    }
    
    /**
     * Gets all incomplete tasks for a user.
     */
    fun getIncompleteTasksForUser(userId: java.util.UUID): List<Task> {
        return taskRepository.findByUserIdAndCompleted(userId, false)
    }
    
    /**
     * Gets tasks for a user by priority.
     */
    fun getTasksByPriority(userId: java.util.UUID, priority: TaskPriority): List<Task> {
        return taskRepository.findByUserIdAndPriority(userId, priority)
    }
    
    /**
     * Calculates the total estimated pomodoros for incomplete tasks.
     */
    fun calculateTotalEstimatedPomodorosForIncompleteTasks(userId: java.util.UUID): Int {
        return taskRepository.sumEstimatedPomodorosForIncompleteTasksByUserId(userId) ?: 0
    }
    
    /**
     * Calculates the total completed pomodoros for a user within a date range.
     */
    fun calculateTotalCompletedPomodorosForUserInDateRange(
        userId: java.util.UUID,
        startDate: java.time.LocalDateTime,
        endDate: java.time.LocalDateTime
    ): Int {
        return taskRepository.sumCompletedPomodorosByUserIdAndDateRange(userId, startDate, endDate) ?: 0
    }
    
    /**
     * Updates daily stats when a task is completed.
     */
    private fun updateDailyStats(task: Task) {
        val today = LocalDate.now()
        val userId = task.user.id
        
        // Get or create daily stats for today
        val dailyStats = dailyStatsRepository.findByUserIdAndDate(userId, today)
            .orElseGet {
                com.hobos.tamadoro.domain.stats.DailyStats(
                    user = task.user,
                    date = today
                )
            }
        
        // Update stats
        dailyStats.incrementCompletedTasks()
        
        // Calculate coins earned (10 coins per completed task)
        val coinsEarned = 10
        dailyStats.addCoinsEarned(coinsEarned)
        
        // Save updated stats
        dailyStatsRepository.save(dailyStats)
    }
}
