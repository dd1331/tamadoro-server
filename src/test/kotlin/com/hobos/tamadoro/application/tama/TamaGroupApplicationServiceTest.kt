package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.application.auth.AppleAuthRequest
import com.hobos.tamadoro.application.auth.AppleUser
import com.hobos.tamadoro.application.auth.AppleUserName
import com.hobos.tamadoro.application.auth.AuthApplicationService
import com.hobos.tamadoro.domain.tamas.TamaService
import com.hobos.tamadoro.domain.tamas.entity.BackgroundEntity
import com.hobos.tamadoro.domain.tamas.entity.TamaCatalog
import com.hobos.tamadoro.domain.tamas.repository.BackgroundRepository
import com.hobos.tamadoro.domain.tamas.repository.GroupRepository
import com.hobos.tamadoro.domain.tamas.repository.TamaCatalogRepository
import com.hobos.tamadoro.domain.tamas.repository.TamaGroupRepository
import com.hobos.tamadoro.domain.user.User
import com.hobos.tamadoro.domain.user.UserRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TamaGroupApplicationServiceTest @Autowired constructor(
    private val tamaCatalogRepository: TamaCatalogRepository,
    private val authService: AuthApplicationService,
    private val userRepository: UserRepository,
    private val tamaGroupService: TamaGroupApplicationService,
    private val tamaService: TamaService,
    private val tamaGroupRepo: TamaGroupRepository,
    private val groupRepo: GroupRepository,
    private val backgroundRepo: BackgroundRepository,
    private val manager: EntityManager

) {

    val users = listOf(
        User(
            providerId = "test-provider"
        )
    )
    val catalogs = listOf(
        TamaCatalog(
            isPremium = false,
            theme = "premium-theme",
            title = "Premium Tama",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
        ),
        TamaCatalog(
            isPremium = false,
            theme = "free-theme",
            title = "Free Tama",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
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

        tamaCatalogRepository.saveAll(catalogs)

        userRepository.saveAll(users)

        backgroundRepo.saveAll(backgrounds)


    }

    @Test
    fun createGroup() {
        val request =
            AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd", AppleUserName("dd", "ddd")), "KR")

        val (user) = authService.authenticateWithApple(request)
        val tama = tamaService.createTama(user.id, "test", catalogs[1].id)
        val tamaGroups = tamaGroupRepo.findAll()
//
        assertTrue { (tamaGroups.isEmpty()) }
        val groups = tamaGroupRepo.findAll()
        assertTrue { groups.size == 0  }

        val dto = CreateGroupRequest("test", "sda", "fds", tama.id, "KR")
        tamaGroupService.createGroup(dto)


        val groups2 = tamaGroupRepo.findAll()
        assertTrue { groups2.size == 1  }
        val tamaGroups2 = tamaGroupRepo.findAll()
        assertTrue { tamaGroups2.size == 1  }

    }

    @Test
    fun assignGroup() {
        val request =
            AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd", AppleUserName("dd", "ddd")), "KR")

        val (user) = authService.authenticateWithApple(request)
        val tama = tamaService.createTama(user.id, "test", catalogs[1].id)
        val dto = CreateGroupRequest("test", "sda", "fds", tama.id, "KR")
        val group = tamaGroupService.createGroup(dto)




        val request2 =
            AppleAuthRequest("token", "213123213123", AppleUser("ddd", "ddd", AppleUserName("dd", "ddd")), "KR")

        val (user2) = authService.authenticateWithApple(request2)



        tamaGroupService.assignGroup(user2.id, user2.progress.activeTamaId, group.id)

        val tamaGroup = tamaGroupRepo.findOneByTamaId(user2.progress.activeTamaId).orElseThrow()
        assertTrue { tamaGroup.group.id == group.id }




    }

}