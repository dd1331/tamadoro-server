package com.hobos.tamadoro.domain.collections

data class BackgroundItem(
    val id: String,
    val name: String,
    val value: String,
    val type: String, // gradient | image
    val focusColor: String? = null,
    val breakColor: String? = null,
    val theme: String, // light | dark | color | nature
    val imagePath: String? = null,
    val isPremium: Boolean
)

data class MusicItem(
    val id: String,
    val name: String,
    val value: String,
    val type: String, // ambient | nature | focus | none
    val volume: Int,
    val isPremium: Boolean
)

data class CharacterItem(
    val id: String,
    val name: String,
    val type: String, // Tamagotchi type
    val size: Int,
    val isPremium: Boolean
)


