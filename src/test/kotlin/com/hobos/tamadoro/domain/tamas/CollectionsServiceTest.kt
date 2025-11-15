package com.hobos.tamadoro.domain.tamas

import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AppleUser
import com.hobos.tamadoro.application.auth.AppleUserName
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.domain.tamas.entity.BackgroundEntity
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.UserCollectionSettingsRepository
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CollectionsServiceTest @Autowired constructor(
    private val authService: AuthApplicationService,
    private val tamaRepo: TamaCatalogRepository,
    private val collectionRepo: UserCollectionSettingsRepository,

    private val collectionsService: CollectionsService,
    private val backgroundRepository: BackgroundRepository
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
        tamaRepo.saveAll(catalogs)
        backgroundRepository.saveAll(backgrounds)
    }


    @Test
    fun `액티브 타마 설정 성공`() {
        val request = AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd",  AppleUserName("dd", "ddd")), "KR")
        val (user) = authService.authenticateWithApple(request)

        val current =  collectionRepo.findOneByUserId(user.id).orElseThrow();


        collectionsService.setActiveCharacter(user.id, catalogs[1].id)

        println("current" + current)
        assert(current.activeTama?.catalog?.id == catalogs[1].id)

    }

    @Test
    fun `배경 설정 성공`() {
        val request =
            AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd", AppleUserName("dd", "ddd")), "KR")
        val (user) = authService.authenticateWithApple(request)

        val current = collectionRepo.findOneByUserId(user.id).orElseThrow();

        assert(current.activeBackground?.url == backgrounds[0].url)

        collectionsService.setActiveBackground(user.id, "test")

        assert(current.activeBackground?.url == "test")
    }

}