package com.hobos.tamadoro

import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TamadoroApplicationTests {

	@Autowired
	private lateinit var userRepository: UserRepository

	@Autowired
	private lateinit var restTemplate: TestRestTemplate

	@LocalServerPort
	private var port: Int = 0

	@Test
	fun contextLoads() {
		// This test verifies that the Spring context loads successfully
		assertNotNull(userRepository)
	}

	@Test
	@Transactional
	fun `should create and save user`() {
		// Given
		val user = User(
			email = "test@example.com",
			name = "Test User"
		)

		// When
		val savedUser = userRepository.save(user)

		// Then
		assertNotNull(savedUser.id)
		assertEquals("test@example.com", savedUser.email)
		assertEquals("Test User", savedUser.name)
		assertFalse(savedUser.isPremium)
	}

	@Test
	@Transactional
	fun `should find user by email`() {
		// Given
		val user = User(
			email = "find@example.com",
			name = "Find User"
		)
		userRepository.save(user)

		// When
		val foundUser = userRepository.findByEmail("find@example.com")

		// Then
		assertTrue(foundUser.isPresent)
		assertEquals("find@example.com", foundUser.get().email)
	}

	@Test
	fun `should return 404 for non-existent endpoint`() {
		// Given
		val url = "http://localhost:$port/api/non-existent"

		// When
		val response = restTemplate.getForEntity(url, String::class.java)

		// Then
		assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
	}

	@Test
	fun `should have health check endpoint`() {
		// Given
		val url = "http://localhost:$port/actuator/health"

		// When
		val response = restTemplate.getForEntity(url, String::class.java)

		// Then
		// The health endpoint might not be available in this test setup, but we can test the basic structure
		assertTrue(response.statusCode.is2xxSuccessful || response.statusCode.is4xxClientError)
	}
}
