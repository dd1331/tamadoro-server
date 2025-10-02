package com.hobos.tamadoro.domain.randombox

import com.hobos.tamadoro.domain.collections.TamaCatalogRepository
import com.hobos.tamadoro.domain.inventory.UserInventoryRepository
import com.hobos.tamadoro.domain.tama.TamaRarity
import com.hobos.tamadoro.domain.tama.TamaService
import com.hobos.tamadoro.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

@Service
class RandomBoxService(
    private val userInventoryRepository: UserInventoryRepository,
    private val userRepository: UserRepository,
    private val tamaService: TamaService,
    private val tamaCatalogRepository: TamaCatalogRepository
) {




    private fun rollReward(box: RandomBox): Reward {
        return box.rewards.random()
    }
}


