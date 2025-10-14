package com.hobos.tamadoro.domain.purchase

import com.hobos.tamadoro.domain.user.Subscription
import com.hobos.tamadoro.domain.user.SubscriptionStatus
import com.hobos.tamadoro.domain.user.SubscriptionType
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.NoSuchElementException
import java.util.UUID

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
        testUser = User(
            providerId = "test-provider-${UUID.randomUUID()}"
        )
        userRepository.save(testUser)
    }

    @Test
    fun `subscribe should create new subscription when user has no active subscription`() {
        val subscriptionType = SubscriptionType.MONTHLY

        val result = subscribe(subscriptionType)

        assertNotNull(result)
        assertEquals(SubscriptionType.MONTHLY, result.type)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)
        assertNotNull(result.startDate)
        assertNotNull(result.endDate)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        assertTrue(savedUser.hasPremium())
        assertEquals(1, savedUser.subscriptions.size)

        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.MONTHLY, subscription.type)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
    }

    @Test
    fun `subscribe should extend existing active subscription when user already has one`() {
        val existingSubscription = Subscription(
            user = testUser,
            type = SubscriptionType.WEEKLY,
            startDate = LocalDateTime.now().minusDays(3),
            endDate = LocalDateTime.now().plusDays(4),
            status = SubscriptionStatus.ACTIVE
        )
        testUser.subscriptions.add(existingSubscription)
        userRepository.save(testUser)

        val result = subscribe(SubscriptionType.MONTHLY)

        assertNotNull(result)
        assertEquals(SubscriptionType.MONTHLY, result.type)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val updatedSubscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.MONTHLY, updatedSubscription.type)
        assertEquals(SubscriptionStatus.ACTIVE, updatedSubscription.status)
        assertTrue(updatedSubscription.endDate!!.isAfter(LocalDateTime.now()))
    }

    @Test
    fun `subscribe should handle weekly subscription type correctly`() {
        val result = subscribe(SubscriptionType.WEEKLY)

        assertEquals(SubscriptionType.WEEKLY, result.type)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.WEEKLY, subscription.type)
    }

    @Test
    fun `subscribe should handle yearly subscription type correctly`() {
        val result = subscribe(SubscriptionType.YEARLY)

        assertEquals(SubscriptionType.YEARLY, result.type)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.YEARLY, subscription.type)
    }

    @Test
    fun `subscribe should handle unlimited subscription type correctly`() {
        val result = subscribe(SubscriptionType.UNLIMITED)

        assertEquals(SubscriptionType.UNLIMITED, result.type)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val subscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionType.UNLIMITED, subscription.type)
        assertNull(subscription.endDate)
    }

    @Test
    fun `subscribe should extend from existing endDate when subscription is still active`() {
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

        subscribe(SubscriptionType.MONTHLY)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val sub = savedUser.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxByOrNull { it.startDate }!!
        val expectedEndDateDate = existingEnd.toLocalDate().plusMonths(1)
        assertNotNull(sub.endDate)
        assertEquals(expectedEndDateDate, sub.endDate!!.toLocalDate())
    }

    @Test
    fun `subscribe should extend from today when existing subscription is expired`() {
        val today = LocalDate.now()
        val expiredEnd = LocalDateTime.of(today.minusDays(1), LocalTime.MAX)
        val expiredSubscription = Subscription(
            user = testUser,
            type = SubscriptionType.MONTHLY,
            startDate = LocalDateTime.now().minusMonths(1),
            endDate = expiredEnd,
            status = SubscriptionStatus.ACTIVE
        )
        testUser.subscriptions.add(expiredSubscription)
        userRepository.save(testUser)

        subscribe(SubscriptionType.WEEKLY)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        val sub = savedUser.subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE && (it.endDate == null || !it.endDate!!.toLocalDate().isBefore(today)) }
            .maxBy { it.startDate }
        val expectedEndDateDate = today.plusWeeks(1)
        assertNotNull(sub.endDate)
        assertEquals(expectedEndDateDate, sub.endDate!!.toLocalDate())
    }

    @Test
    fun `subscribe should throw exception when user not found`() {
        val nonExistentUserId = UUID.randomUUID()
        val command = SubscribeCommand(
            type = SubscriptionType.MONTHLY,
            purchase = basicPurchaseRecord()
        )

        assertThrows(NoSuchElementException::class.java) {
            purchaseService.subscribe(nonExistentUserId, command)
        }
    }

    @Test
    fun `subscriptionStatus should return correct status after subscription`() {
        val result = subscribe(SubscriptionType.MONTHLY)

        assertNotNull(result.startDate)
        assertNotNull(result.status)
        assertEquals(SubscriptionStatus.ACTIVE, result.status)
        assertEquals(SubscriptionType.MONTHLY, result.type)

        val statusResult = purchaseService.subscriptionStatus(testUser.id)
        assertNotNull(statusResult)
        assertEquals(SubscriptionType.MONTHLY, statusResult!!.type)
        assertEquals(SubscriptionStatus.ACTIVE, statusResult.status)
    }

    @Test
    fun `subscriptionHistory should return correct history`() {
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

        val history = purchaseService.subscriptionHistory(testUser.id)

        assertEquals(2, history.size)
        assertEquals("monthly", history[0].type)
        assertEquals("weekly", history[1].type)
    }

    @Test
    fun `cancel should cancel active subscription`() {
        subscribe(SubscriptionType.MONTHLY)

        val result = purchaseService.cancel(testUser.id)

        assertNull(result)

        val savedUser = userRepository.findById(testUser.id).orElseThrow()
        assertFalse(savedUser.hasPremium())
        val cancelledSubscription = savedUser.subscriptions.first()
        assertEquals(SubscriptionStatus.CANCELLED, cancelledSubscription.status)
    }

    private fun subscribe(
        type: SubscriptionType,
        purchasedAt: LocalDateTime = LocalDateTime.now()
    ) = purchaseService.subscribe(
        testUser.id,
        SubscribeCommand(
            type = type,
            purchase = basicPurchaseRecord(
                transactionId = "txn-${type.name.lowercase()}-${UUID.randomUUID()}",
                productId = "product-${type.name.lowercase()}",
                purchasedAt = purchasedAt
            )
        )
    )

    private fun basicPurchaseRecord(
        transactionId: String = "txn-${UUID.randomUUID()}",
        productId: String = "product-${UUID.randomUUID()}",
        purchasedAt: LocalDateTime = LocalDateTime.now()
    ) = PurchaseRecord(
        platform = PurchasePlatform.APPLE,
        productId = productId,
        transactionId = transactionId,
        purchaseToken = null,
        receiptData = null,
        purchasedAt = purchasedAt,
        expiresAt = null,
        priceAmount = null,
        currencyCode = null
    )
}
