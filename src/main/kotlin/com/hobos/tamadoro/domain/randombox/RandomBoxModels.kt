package com.hobos.tamadoro.domain.randombox

data class RandomBox(
    val id: String,
    val name: String,
    val price: Int,
    val currency: String, // coins | gems
    val description: String,
    val rarity: String, // common | rare | epic | legendary
    val rewards: List<Reward>
)

data class Reward(
    val type: String, // tamagotchi | coin | gem
    val rarity: String,
    val name: String,
    val icon: String,
    val amount: Int? = null
)


