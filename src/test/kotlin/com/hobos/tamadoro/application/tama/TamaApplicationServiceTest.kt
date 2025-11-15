package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.entity.UserTama
import com.hobos.tamadoro.domain.tamas.TamaService
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
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

    private lateinit var tamaCatalog: TamaCatalog

    @BeforeEach
    fun setUp() {
        user = userRepository.save(
            User(
                providerId = "test-provider"
            )
        )

        tamaCatalog = tamaCatalogRepository.save(
            TamaCatalog(
                isPremium = true,
                theme = "premium-theme",
                title = "Premium Tama",
                url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
            )
        )
        tamaCatalogRepository.save(
            TamaCatalog(
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
                catalog = tamaCatalog,
                name = "test-tama"
            )
        )
        val tamas = tamaService.getAllTamasForUser(user.id)

        assertEquals(1, tamas.size)
    }


}
