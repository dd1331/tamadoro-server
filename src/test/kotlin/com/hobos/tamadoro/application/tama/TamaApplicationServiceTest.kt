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
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TamaApplicationServiceTest @Autowired constructor(
    private val tamaCatalogRepository: TamaCatalogRepository,
    private val userRepository: UserRepository,

) {

    val users = listOf(User(
        providerId = "test-provider"
    ))
    val catalogs = listOf(TamaCatalog(
        isPremium = true,
        theme = "premium-theme",
        title = "Premium Tama",
        url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
        ),
        TamaCatalog(
            isPremium = false,
            theme = "free-theme",
            title = "Free Tama",
            url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10"
        ))

    @BeforeEach
    fun setUp() {

        tamaCatalogRepository.saveAll(catalogs)

        userRepository.saveAll(users)


    }



    @Test
    fun assignGroup() {




    }


}
