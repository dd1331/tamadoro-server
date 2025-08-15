package com.hobos.tamadoro.domain.randombox

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
    private val tamaService: TamaService
) {
    private val catalog = listOf(
        RandomBox(
            id = "rb1",
            name = "Starter Box",
            price = 100,
            currency = "coins",
            description = "A box with basic rewards",
            rarity = "common",
            rewards = listOf(
                Reward("coin", "common", "Coins", "coin", 50),
                Reward("gem", "rare", "Gems", "gem", 1),
                Reward("tama", "rare", "Random Pet", "egg")
            )
        )
    )

    fun list(userId: UUID): List<RandomBox> = catalog

    fun purchase(userId: UUID, id: String): List<Reward> {
        val box = catalog.firstOrNull { it.id == id } ?: throw IllegalArgumentException("Random box not found")
        val inventory = userInventoryRepository.findByUserId(userId)
            .orElseThrow { NoSuchElementException("Inventory not found") }

        // Deduct price
        if (box.currency == "coins") {
            if (!inventory.removeCoins(box.price)) throw IllegalArgumentException("Insufficient coins")
        } else {
            if (!inventory.removeGems(box.price)) throw IllegalArgumentException("Insufficient gems")
        }
        userInventoryRepository.save(inventory)

        // Roll a reward
        val reward = rollReward(box)
        if (reward.type == "tama") {
            val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
            val rarity = when (Random.nextInt(100)) {
                in 0..50 -> TamaRarity.COMMON
                in 51..80 -> TamaRarity.RARE
                in 81..95 -> TamaRarity.EPIC
                in 96..98 -> TamaRarity.LEGENDARY
                else -> TamaRarity.MYTHIC
            }
            tamaService.createTama(user, name = "Eggy", type = com.hobos.tamadoro.domain.tama.TamaType.values().random(), rarity = rarity)
        } else if (reward.type == "coin") {
            inventory.addCoins(reward.amount ?: 0)
            userInventoryRepository.save(inventory)
        } else if (reward.type == "gem") {
            inventory.addGems(reward.amount ?: 0)
            userInventoryRepository.save(inventory)
        }

        return listOf(reward)
    }

    private fun rollReward(box: RandomBox): Reward {
        return box.rewards.random()
    }
}


