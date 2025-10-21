package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.application.auth.GuestLoginRequest
import com.hobos.tamadoro.domain.tamas.TamaCatalogEntity
import com.hobos.tamadoro.domain.tamas.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.UserTama
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.tama.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TamaApplicationServiceTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tamaCatalogRepository: TamaCatalogRepository

    @Autowired
    private lateinit var userTamaRepository: UserTamaRepository

    @Autowired
    private lateinit var tamaService: TamaService

    @Autowired
    private lateinit var authApplicationService: AuthApplicationService

    private lateinit var user: User

    private lateinit var tamaCatalogEntity: TamaCatalogEntity

    @BeforeEach
    fun setUp() {
        user = userRepository.save(
            User(
                providerId = "test-provider"
            )
        )

        tamaCatalogEntity = tamaCatalogRepository.save(
            TamaCatalogEntity(
                isPremium = true,
                theme = "premium-theme",
                title = "Premium Tama",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
            )
        )
        tamaCatalogRepository.save(
            TamaCatalogEntity(
                isPremium = false,
                theme = "free-theme",
                title = "Free Tama",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
            )
        )
    }

    @Test
    fun `get user tamas`() {
        userTamaRepository.save(
            UserTama(
                user = user,
                tama = tamaCatalogEntity,
            )
        )
        val tamas = tamaService.getAllTamasForUser(user.id)

        assertEquals(1, tamas.size)
    }

    @Test
    fun `should assign default tama on guest signup`() {
        val guestSignup = authApplicationService.loginAsGuest(
            GuestLoginRequest(countryCode = "KR")
        )

        val tamas = tamaService.getAllTamasForUser(guestSignup.user.id)

        assertEquals(1, tamas.size)
        val savedTama = tamas.first()
        assertEquals(guestSignup.user.id, savedTama.user?.id)
        assertTrue(savedTama.isActive)
    }
}
