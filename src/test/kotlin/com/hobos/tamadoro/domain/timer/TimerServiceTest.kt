package com.hobos.tamadoro.domain.timer

import com.hobos.tamadoro.domain.stats.DailyStats
import com.hobos.tamadoro.domain.stats.DailyStatsRepository
import com.hobos.tamadoro.domain.task.Task
import com.hobos.tamadoro.domain.task.TaskRepository
import com.hobos.tamadoro.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TimerServiceTest {

    @Mock
    private lateinit var timerSessionRepository: TimerSessionRepository

    @Mock
    private lateinit var timerSettingsRepository: TimerSettingsRepository

    @Mock
    private lateinit var taskRepository: TaskRepository

    @Mock
    private lateinit var dailyStatsRepository: DailyStatsRepository

    @InjectMocks
    private lateinit var timerService: TimerService

    @Captor
    private lateinit var timerSessionCaptor: ArgumentCaptor<TimerSession>

    @Captor
    private lateinit var dailyStatsCaptor: ArgumentCaptor<DailyStats>

    private lateinit var user: User
    private lateinit var timerSettings: TimerSettings
    private lateinit var workSession: TimerSession
    private lateinit var task: Task
    private lateinit var dailyStats: DailyStats

    @BeforeEach
    fun setUp() {
        user = User(
            id = UUID.randomUUID(),
            providerId = UUID.randomUUID().toString(),
        )

        timerSettings = TimerSettings(
            user = user,
            workTime = 25,
            shortBreakTime = 5,
            longBreakTime = 15,
            longBreakInterval = 4
        )

        workSession = TimerSession(
            id = UUID.randomUUID(),
            user = user,
            type = TimerSessionType.WORK,
            duration = 25
        )

        task = Task(
            id = UUID.randomUUID(),
            user = user,
            title = "Test Task"
        )

        dailyStats = DailyStats(
            user = user,
            date = LocalDate.now()
        )
    }

    @Test
    fun `should create timer session with correct duration based on settings`() {
        // Given
        `when`(timerSettingsRepository.findByUserId(user.id)).thenReturn(Optional.of(timerSettings))
        `when`(timerSessionRepository.save(any())).thenReturn(workSession)

        // When
        val result = timerService.createTimerSession(user, TimerSessionType.WORK)

        // Then
        verify(timerSessionRepository).save(timerSessionCaptor.capture())
        val capturedSession = timerSessionCaptor.value

        assertEquals(user, capturedSession.user)
        assertEquals(TimerSessionType.WORK, capturedSession.type)
        assertEquals(25, capturedSession.duration) // Work time from settings
        assertFalse(capturedSession.completed)
        assertNull(capturedSession.completedAt)
    }

    @Test
    fun `should create default timer settings if none exist`() {
        // Given
        `when`(timerSettingsRepository.findByUserId(user.id)).thenReturn(Optional.empty())
        `when`(timerSettingsRepository.save(any())).thenReturn(timerSettings)
        `when`(timerSessionRepository.save(any())).thenReturn(workSession)

        // When
        timerService.createTimerSession(user, TimerSessionType.WORK)

        // Then
        verify(timerSettingsRepository).save(any())
    }

    @Test
    fun `should complete timer session and update related entities`() {
        // Given
        val sessionId = UUID.randomUUID()
        val taskId = UUID.randomUUID()

        val session = TimerSession(
            id = sessionId,
            user = user,
            type = TimerSessionType.WORK,
            duration = 25,
            taskId = taskId,
            startedAt = LocalDateTime.now().minusMinutes(25)
        )

        `when`(timerSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        `when`(taskRepository.findById(taskId)).thenReturn(Optional.of(task))
        `when`(dailyStatsRepository.findByUserIdAndDate(user.id, LocalDate.now())).thenReturn(Optional.of(dailyStats))
        `when`(timerSessionRepository.save(any())).thenReturn(session)
        `when`(taskRepository.save(any())).thenReturn(task)
        `when`(dailyStatsRepository.save(any())).thenReturn(dailyStats)

        // When
        val result = timerService.completeTimerSession(sessionId)

        // Then
        assertTrue(result.completed)
        assertNotNull(result.completedAt)

        // Verify task was updated
        verify(taskRepository).save(any())

        // Verify daily stats were updated
        verify(dailyStatsRepository).save(dailyStatsCaptor.capture())
        val capturedStats = dailyStatsCaptor.value

        assertEquals(1, capturedStats.completedPomodoros)
        assertTrue(capturedStats.totalFocusTime > 0)
        assertTrue(capturedStats.coinsEarned > 0)
    }

    @Test
    fun `should not update task or stats when completing non-work session`() {
        // Given
        val sessionId = UUID.randomUUID()

        val session = TimerSession(
            id = sessionId,
            user = user,
            type = TimerSessionType.SHORT_BREAK,
            duration = 5
        )

        `when`(timerSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        `when`(timerSessionRepository.save(any())).thenReturn(session)

        // When
        val result = timerService.completeTimerSession(sessionId)

        // Then
        assertTrue(result.completed)

        // Verify task was not updated
        verify(taskRepository, never()).save(any())

        // Verify daily stats were not updated
        verify(dailyStatsRepository, never()).save(any())
    }

    @Test
    fun `should update timer settings`() {
        // Given
        val userId = user.id

        `when`(timerSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(timerSettings))
        `when`(timerSettingsRepository.save(any())).thenReturn(timerSettings)

        // When
        val result = timerService.updateTimerSettings(
            userId = userId,
            shortBreakTime = 10,
            longBreakTime = 20,
            autoStartBreaks = true
        )

        // Then
        assertEquals(10, result.shortBreakTime)
        assertEquals(20, result.longBreakTime)
        assertTrue(result.autoStartBreaks)
    }

    @Test
    fun `should calculate total focus time for date`() {
        // Given
        val userId = user.id
        val date = LocalDate.now()
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1)

        val session1 = TimerSession(
            user = user,
            type = TimerSessionType.WORK,
            duration = 25,
            completed = true,
            startedAt = startOfDay.plusHours(1),
            completedAt = startOfDay.plusHours(1).plusMinutes(25)
        )

        val session2 = TimerSession(
            user = user,
            type = TimerSessionType.WORK,
            duration = 25,
            completed = true,
            startedAt = startOfDay.plusHours(2),
            completedAt = startOfDay.plusHours(2).plusMinutes(25)
        )

        val sessions = listOf(session1, session2)

        `when`(timerSessionRepository.findByUserIdAndStartedAtBetween(userId, startOfDay, endOfDay))
            .thenReturn(sessions)

        // When
        val result = timerService.calculateTotalFocusTimeForDate(userId, date)

        // Then
        assertEquals(50, result) // 25 + 25 = 50 minutes
    }
}
