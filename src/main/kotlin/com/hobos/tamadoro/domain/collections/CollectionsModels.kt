package com.hobos.tamadoro.domain.collections

open class Item(
    open val id: String,
    open val title: String,
    open val theme: String,
    open val url: String,
    open val isPremium: Boolean
)


data class BackgroundItem(
    override val id: String,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
) : Item(id, title, theme, url, isPremium)


data class MusicItem(
    override val id: String,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
    val resource: String
) : Item(id, title, theme, url, isPremium)

data class CharacterItem(
    override val id: String,
    override val title: String,
    override val url: String,
    override val theme: String, // Tamagotchi type
    override val isPremium: Boolean,
): Item(id, title, theme, url, isPremium)


