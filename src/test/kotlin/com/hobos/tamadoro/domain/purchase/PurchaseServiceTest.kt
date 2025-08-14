package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.user.*
import com.hobos.tamadoro.application.purchase.SubscriptionStatusDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseServiceTest {

    @Autowired
    private lateinit var purchaseService: PurchaseService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        testUser = User(
            providerId = "test-provider-${UUID.randomUUID()}",
            isPremium = false
        )
        userRepository.save(testUser)
    }

    @Test
    fun `subscribe should create new subscription when user has no active subscription`() {
        // Given
        val subscriptionType = SubscriptionType.MONTHLY

        // When
        val result = purchaseService.subscribe(testUser.id, subscriptionType)

        // Then
        assertNotNull(result)
        assertEquals("monthly", result.type)
        assertEquals("active", result.status)
        assertNotNull(result.startDate)
        assertNotNull(result.endDate)

        // DB에서 실제 데이터 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        assertTrue(savedUser.isPremium)
        assertEquals(1, savedUser.subscriptions.size)
        
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.MONTHLY, subscription.type)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
    }

    @Test
    fun `subscribe should extend existing active subscription when user already has one`() {
        // Given - 기존 구독 생성
        val existingType = SubscriptionType.WEEKLY
        val existingSubscription = Subscription(
            user = testUser,
            type = existingType,
            startDate = LocalDateTime.now().minusDays(3),
            endDate = LocalDateTime.now().plusDays(4),
            status = SubscriptionStatus.ACTIVE
        )
        testUser.subscriptions.add(existingSubscription)
        userRepository.save(testUser)

        val newSubscriptionType = SubscriptionType.MONTHLY

        // When
        val result = purchaseService.subscribe(testUser.id, newSubscriptionType)

        // Then
        assertNotNull(result)
        assertEquals("monthly", result.type)
        assertEquals("active", result.status)

        // DB에서 실제 데이터 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val updatedSubscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.MONTHLY, updatedSubscription.type)
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.status)
        assertTrue(updatedSubscription.endDate!!.isAfter(LocalDateTime.now()))
    }

    @Test
    fun `subscribe should handle weekly subscription type correctly`() {
        // Given
        val subscriptionType = SubscriptionType.WEEKLY

        // When
        val result = purchaseService.subscribe(testUser.id, subscriptionType)

        // Then
        assertEquals("weekly", result.type)
        assertEquals("active", result.status)
        
        // DB 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.WEEKLY, subscription.type)
    }

    @Test
    fun `subscribe should handle yearly subscription type correctly`() {
        // Given
        val subscriptionType = SubscriptionType.YEARLY

        // When
        val result = purchaseService.subscribe(testUser.id, subscriptionType)

        // Then
        assertEquals("yearly", result.type)
        assertEquals("active", result.status)
        
        // DB 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.YEARLY, subscription.type)
    }

    @Test
    fun `subscribe should handle unlimited subscription type correctly`() {
        // Given
        val subscriptionType = SubscriptionType.UNLIMITED

        // When
        val result = purchaseService.subscribe(testUser.id, subscriptionType)

        // Then
        assertEquals("unlimited", result.type)
        assertEquals("active", result.status)
        
        // DB 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.UNLIMITED, subscription.type)
        assertNull(subscription.endDate) // 무제한은 endDate가 null
    }

    @Test
    fun `subscribe should extend from existing endDate when subscription is still active`() {
        // Given
        val today = LocalDate.now()
        val existingEnd = LocalDateTime.of(today.plusDays(5), LocalTime.MAX)
        val existingSubscription = Subscription(
            user = testUser,
            type = SubscriptionType.WEEKLY,
            startDate = LocalDateTime.now().minusDays(3),
            endDate = existingEnd,
            status = SubscriptionStatus.ACTIVE
        )
        testUser.subscriptions.add(existingSubscription)
        userRepository.save(testUser)

        // When: extend by MONTHLY
        purchaseService.subscribe(testUser.id, SubscriptionType.MONTHLY)

        // Then
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val sub = savedUser.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }!!
        val expectedEndDateDate = existingEnd.toLocalDate().plusMonths(1)
        assertNotNull(sub.endDate)
        assertEquals(expectedEndDateDate, sub.endDate!!.toLocalDate(), "Active subscription should extend from its end date by +1 month")
    }

    @Test
    fun `subscribe should extend from today when existing subscription is expired`() {
        // Given
        val today = LocalDate.now()
        val expiredEnd = LocalDateTime.of(today.minusDays(1), LocalTime.MAX)
        val expiredSubscription = Subscription(
            user = testUser,
            type = SubscriptionType.MONTHLY,
            startDate = LocalDateTime.now().minusMonths(1),
            endDate = expiredEnd,
            status = SubscriptionStatus.ACTIVE // logically active but past endDate -> treated as expired by service logic
        )
        testUser.subscriptions.add(expiredSubscription)
        userRepository.save(testUser)

        // When: extend by WEEKLY
        purchaseService.subscribe(testUser.id, SubscriptionType.WEEKLY)

        // Then
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val sub = savedUser.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxBy { it.startDate }
        val expectedEndDateDate = today.plusWeeks(1)
        assertNotNull(sub.endDate)
        assertEquals(expectedEndDateDate, sub.endDate!!.toLocalDate(), "Expired subscription should extend from today by +1 week")
    }

    @Test
    fun `subscribe should throw exception when user not found`() {
        // Given
        val nonExistentUserId = UUID.randomUUID()
        val subscriptionType = SubscriptionType.MONTHLY

        // When & Then
        assertThrows(NoSuchElementException::class.java) {
            purchaseService.subscribe(nonExistentUserId, subscriptionType)
        }
    }

    @Test
    fun `subscriptionStatus should return correct status after subscription`() {
        // Given
        val subscriptionType = SubscriptionType.MONTHLY

        // When
        val result = purchaseService.subscribe(testUser.id, subscriptionType)

        // Then
        assertNotNull(result.startDate)
        assertNotNull(result.status)
        assertEquals("active", result.status)
        assertEquals("monthly", result.type)

        // subscriptionStatus 메서드로도 확인
        val statusResult = purchaseService.subscriptionStatus(testUser.id)
        assertNotNull(statusResult)
        assertEquals("monthly", statusResult!!.type)
        assertEquals("active", statusResult.status)
    }

    @Test
    fun `subscriptionHistory should return correct history`() {
        // Given - 여러 구독 생성
        val weeklySubscription = Subscription(
            user = testUser,
            type = SubscriptionType.WEEKLY,
            startDate = LocalDateTime.now().minusDays(10),
            endDate = LocalDateTime.now().minusDays(3),
            status = SubscriptionStatus.EXPIRED
        )
        val monthlySubscription = Subscription(
            user = testUser,
            type = SubscriptionType.MONTHLY,
            startDate = LocalDateTime.now().minusDays(2),
            endDate = LocalDateTime.now().plusDays(28),
            status = SubscriptionStatus.ACTIVE
        )
        testUser.subscriptions.addAll(listOf(weeklySubscription, monthlySubscription))
        userRepository.save(testUser)

        // When
        val history = purchaseService.subscriptionHistory(testUser.id)

        // Then
        assertEquals(2, history.size)
        assertEquals("monthly", history[0].type) // 최신 구독이 먼저
        assertEquals("weekly", history[1].type)
    }

    @Test
    fun `cancel should cancel active subscription`() {
        // Given - 활성 구독 생성
        val subscriptionType = SubscriptionType.MONTHLY
        purchaseService.subscribe(testUser.id, subscriptionType)

        // When
        val result = purchaseService.cancel(testUser.id)

        // Then
        // cancel 후에는 ACTIVE 구독이 없으므로 null 반환
        assertNull(result)

        // DB 확인
        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        assertFalse(savedUser.isPremium)
        val cancelledSubscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionStatus.CANCELLED, cancelledSubscription.status)
    }
}