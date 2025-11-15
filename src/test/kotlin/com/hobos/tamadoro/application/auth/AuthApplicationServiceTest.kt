package com.hobos.tamadoro.application.auth

import com.hobos.tamadoro.domain.tamas.entity.BackgroundEntity
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalogEntity
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuthApplicationServiceTest @Autowired constructor(
    private val authService: AuthApplicationService,
    private val tamaRepo: TamaCatalogRepository,
    private val collectionRepo: UserCollectionSettingsRepository,
    private val backgroundRepository: BackgroundRepository
) {



    val catalog = TamaCatalogEntity(
        theme = "classic",
        title = "Tamadoro",
        url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
        isPremium = false
    )
    val background = BackgroundEntity(
        theme = "classic",
        title = "Tamadoro",
        url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3sIx-YjVltyxbaJaDLFXqJEYU1Dxqu4n01Q&s",
    )
    @BeforeEach
    fun setUp() {
        tamaRepo.save(catalog)
        backgroundRepository.save(background)
    }

    @Test
    fun `가입시 기본 타마와 배경이 설정되어야 함`() {
        val request = AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd",  AppleUserName("dd", "ddd")), "KR")
        val (user) = authService.authenticateWithApple(request)
        val userCollectionSettings = collectionRepo.findByUser_Id(user.id).orElseThrow()
        assertEquals(userCollectionSettings.activeBackground?.theme, "classic")
        assertEquals(userCollectionSettings.activeTama?.id, catalog.id)
    }
}
