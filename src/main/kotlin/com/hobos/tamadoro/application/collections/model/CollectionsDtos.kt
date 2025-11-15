package com.hobos.tamadoro.application.collections.model


open class Item(
    open val id: Long,
    open val title: String,
    open val theme: String,
    open val isPremium: Boolean,
    open val url: String?
)

data class BackgroundItem(
    override val id: Long,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
) : Item(id, title, theme, isPremium, url)

data class MusicItem(
    override val id: Long,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
    val resource: String
) : Item(id, title, theme, isPremium, url)

data class TamaItem(
    override val id: Long,
    override val title: String,
    override val theme: String,
    override val isPremium: Boolean,
) : Item(id, title, theme, isPremium, url = null)


