package com.hobos.tamadoro.application.tama

import com.hobos.tamadoro.domain.collections.StageName
import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.collections.TamaCatalogStageEntity
import com.hobos.tamadoro.domain.collections.UserTama
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.tama.UserTamaRepository
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

    private lateinit var user: User

    private lateinit var tamaCatalogEntity: TamaCatalogEntity

    @BeforeEach
    fun setUp() {

        user = userRepository.save(User(
            providerId = "TODO()",
        ))


        tamaCatalogEntity = tamaCatalogRepository.save(
            TamaCatalogEntity(
                isPremium = true,
                theme = "TODO()",
                title = "TODO()",
                url = "TODO()"
            )
        )
        tamaCatalogRepository.save(
            TamaCatalogEntity(
                isPremium = false,
                theme = "TODO()",
                title = "TODO()",
                url = "TODO()"
            )
        )

    }
    @Test
    fun `get user tamas`(){

        println("@@@+" + user)
        userTamaRepository.save(UserTama(
            user = user,
            tama = tamaCatalogEntity,
        ))
        val tamas = tamaService.getAllTamasForUser(user.id)

        println(tamas[0])
        assertEquals(tamas.size, 1)
    }

    @Test
    fun `get default tama if there is no`(){
        // TODO: 가입시 기본타마 주는거 테스트
        val tamas = tamaService.getAllTamasForUser(user.id)

        assertEquals(tamas.size, 0)
    }

}