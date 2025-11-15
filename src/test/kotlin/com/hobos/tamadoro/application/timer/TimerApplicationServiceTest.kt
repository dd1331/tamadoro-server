package com.hobos.tamadoro.application.timer

import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AppleUser
import com.hobos.tamadoro.application.auth.AppleUserName
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.domain.tamas.TamaService
import com.hobos.tamadoro.domain.tamas.entity.BackgroundEntity
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.UserTamaRepository
import com.hobos.tamadoro.domain.timer.TimerSessionType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TimerApplicationServiceTest  @Autowired constructor(
    private val authService: AuthApplicationService,
    private val catalogRepo: TamaCatalogRepository,
    private val tamaRepo: UserTamaRepository,
    private val timerService: TimerApplicationService,
    private val tamaService: TamaService,
    private val backgroundRepository: BackgroundRepository,
    private val entityManager: EntityManager
) {
    val catalogs = listOf(
        TamaCatalog(
            theme = "classic",
            title = "Tamadoro",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
            isPremium = false
        ),
        TamaCatalog(
            theme = "classic2",
            title = "Tamadoro2",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
            isPremium = false
        )
    )
    val backgrounds = listOf(
        BackgroundEntity(
            theme = "classic",
            title = "Tamadoro",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
        ),
        BackgroundEntity(
            theme = "classic2",
            title = "Tamadoro2",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
        )
    )
    @BeforeEach
    fun setUp() {
        catalogRepo.saveAll(catalogs)
        backgroundRepository.saveAll(backgrounds)
    }
    @Test
    fun `completeSession`() {
        val request =
            AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd", AppleUserName("dd", "ddd")), "KR")

        val (user) = authService.authenticateWithApple(request)



        val tamas = tamaRepo.findByUserId(user.id)

        assertEquals(1, tamas.size)
        tamaService.createTama(user.id, "test", catalogs[1].id)
        val tamas2 = tamaRepo.findByUserId(user.id)

        tamaService.setActiveTama(user.id, tamas2[1].id)


        assertEquals(2, tamas2.size)



        assertTrue(!tamas2[0].isActive)
        assertTrue(tamas2[1].isActive)
        assertTrue(tamas2[1].experience == 0)


        entityManager.flush()
        entityManager.clear()

        timerService.completeSession(user.id, TimerSessionType.FOCUS, 25)
        val completedTamas = tamaRepo.findByUserId(user.id)
        assertTrue(completedTamas[1].experience > 0)



    }
}