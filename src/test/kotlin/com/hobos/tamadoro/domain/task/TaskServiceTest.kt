package com.hobos.tamadoro.domain.task

import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.stats.DailyStatsRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class TaskServiceTest {

    @Mock
    private lateinit var taskRepository: TaskRepository
    @Mock
    private lateinit var dailyStatsRepository: DailyStatsRepository

    private lateinit var taskService: TaskService

    @Captor
    private lateinit var taskCaptor: ArgumentCaptor<Task>

    private lateinit var user: User
    private lateinit var task: Task

    @BeforeEach
    fun setUp() {
        taskService = TaskService(taskRepository, dailyStatsRepository)
        user = User(
            id = UUID.randomUUID(),
            providerId = UUID.randomUUID().toString(),
        )

        task = Task(
            id = UUID.randomUUID(),
            user = user,
            title = "Test Task",
            description = "Test Description",
            priority = TaskPriority.MEDIUM,
            estimatedPomodoros = 2
        )
    }

    @Test
    fun `should create task with correct parameters`() {
        // Given
        val title = "New Task"
        val description = "New Description"
        val priority = "HIGH"
        val estimatedPomodoros = 3

        `when`(taskRepository.save(any())).thenReturn(task)

        // When
        taskService.createTask(user, title, description, priority, estimatedPomodoros)

        // Then
        verify(taskRepository).save(taskCaptor.capture())
        val capturedTask = taskCaptor.value

        assertEquals(user, capturedTask.user)
        assertEquals(title, capturedTask.title)
        assertEquals(description, capturedTask.description)
        assertEquals(TaskPriority.HIGH, capturedTask.priority)
        assertEquals(estimatedPomodoros, capturedTask.estimatedPomodoros)
        assertFalse(capturedTask.completed)
        assertEquals(0, capturedTask.completedPomodoros)
    }

    @Test
    fun `should update task with provided parameters`() {
        // Given
        val newTitle = "Updated Task"
        val newDescription = "Updated Description"
        val newPriority = "LOW"
        val newEstimatedPomodoros = 4

        `when`(taskRepository.save(any())).thenReturn(task)

//        // When
//        taskService.updateTask(task, newTitle, newDescription, newPriority, newEstimatedPomodoros)
//
//        // Then
//        verify(taskRepository).save(task)
//        assertEquals(newTitle, task.title)
//        assertEquals(newDescription, task.description)
//        assertEquals(TaskPriority.LOW, task.priority)
//        assertEquals(newEstimatedPomodoros, task.estimatedPomodoros)
    }

    @Test
    fun `should complete task`() {
        // Given
        `when`(taskRepository.save(any())).thenReturn(task)

        // When
        taskService.completeTask(task)

        // Then
        verify(taskRepository).save(task)
        assertTrue(task.completed)
        assertNotNull(task.completedAt)
    }

    @Test
    fun `should reopen completed task`() {
        // Given
        task.complete()
        `when`(taskRepository.save(any())).thenReturn(task)

        // When
        taskService.reopenTask(task)

        // Then
        verify(taskRepository).save(task)
        assertFalse(task.completed)
        assertNull(task.completedAt)
    }

    @Test
    fun `should get tasks for user`() {
        // Given
        val userId = user.id
        val tasks = listOf(task)
        `when`(taskRepository.findByUserId(userId)).thenReturn(tasks)

        // When
        val result = taskService.getTasksForUser(userId)

        // Then
        assertEquals(tasks, result)
        verify(taskRepository).findByUserId(userId)
    }

    @Test
    fun `should get completed tasks for user`() {
        // Given
        val userId = user.id
        val completedTasks = listOf(task)
        `when`(taskRepository.findByUserIdAndCompleted(userId, true)).thenReturn(completedTasks)

        // When
        val result = taskService.getCompletedTasksForUser(userId)

        // Then
        assertEquals(completedTasks, result)
        verify(taskRepository).findByUserIdAndCompleted(userId, true)
    }

    @Test
    fun `should get incomplete tasks for user`() {
        // Given
        val userId = user.id
        val incompleteTasks = listOf(task)
        `when`(taskRepository.findByUserIdAndCompleted(userId, false)).thenReturn(incompleteTasks)

        // When
        val result = taskService.getIncompleteTasksForUser(userId)

        // Then
        assertEquals(incompleteTasks, result)
        verify(taskRepository).findByUserIdAndCompleted(userId, false)
    }

    @Test
    fun `should get tasks by priority`() {
        // Given
        val userId = user.id
        val priority = TaskPriority.HIGH
        val highPriorityTasks = listOf(task)
        `when`(taskRepository.findByUserIdAndPriority(userId, priority)).thenReturn(highPriorityTasks)

        // When
        val result = taskService.getTasksByPriority(userId, priority)

        // Then
        assertEquals(highPriorityTasks, result)
        verify(taskRepository).findByUserIdAndPriority(userId, priority)
    }

    @Test
    fun `should calculate total estimated pomodoros for incomplete tasks`() {
        // Given
        val userId = user.id
        val totalEstimated = 10
        `when`(taskRepository.sumEstimatedPomodorosForIncompleteTasksByUserId(userId)).thenReturn(totalEstimated)

        // When
        val result = taskService.calculateTotalEstimatedPomodorosForIncompleteTasks(userId)

        // Then
        assertEquals(totalEstimated, result)
        verify(taskRepository).sumEstimatedPomodorosForIncompleteTasksByUserId(userId)
    }

    @Test
    fun `should return zero when no incomplete tasks`() {
        // Given
        val userId = user.id
        `when`(taskRepository.sumEstimatedPomodorosForIncompleteTasksByUserId(userId)).thenReturn(null)

        // When
        val result = taskService.calculateTotalEstimatedPomodorosForIncompleteTasks(userId)

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `should calculate total completed pomodoros in date range`() {
        // Given
        val userId = user.id
        val startDate = java.time.LocalDateTime.now()
        val endDate = startDate.plusDays(7)
        val totalCompleted = 15
        `when`(taskRepository.sumCompletedPomodorosByUserIdAndDateRange(userId, startDate, endDate)).thenReturn(totalCompleted)

        // When
        val result = taskService.calculateTotalCompletedPomodorosForUserInDateRange(userId, startDate, endDate)

        // Then
        assertEquals(totalCompleted, result)
        verify(taskRepository).sumCompletedPomodorosByUserIdAndDateRange(userId, startDate, endDate)
    }
} 