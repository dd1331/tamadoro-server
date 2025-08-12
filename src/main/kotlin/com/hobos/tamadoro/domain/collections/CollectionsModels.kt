package com.hobos.tamadoro.domain.collections

// Item 클래스의 url을 nullable로 유지하여 재사용성을 높였습니다.
open class Item(
    open val id: String,
    open val title: String,
    open val theme: String,
    open val isPremium: Boolean,
    open val url: String? // url을 nullable로 선언하여 모든 하위 클래스에서 필수로 사용하지 않도록 함
)

data class BackgroundItem(
    override val id: String,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
) : Item(id, title, theme, isPremium, url)

data class MusicItem(
    override val id: String,
    override val title: String,
    override val theme: String,
    override val url: String,
    override val isPremium: Boolean,
    val resource: String
) : Item(id, title, theme, isPremium, url)

// CharacterItem은 Item 클래스를 상속받으면서 url을 사용하지 않으므로,
// Item 생성자에 url = null을 명시적으로 전달합니다.
data class TamagotchiItem(
    override val id: String,
    override val title: String,
    override val theme: String,
    override val isPremium: Boolean,
    val stages: List<Stage>,
    val happiness: Int,
    val hunger: Int,
    val energy: Int
) : Item(id, title, theme, isPremium, url = null) // url에 null을 전달하여 Item 생성자를 호출

data class Stage(
    val name: StageName,
    val experience: Int,
    val maxExperience: Int,
    val level: Int,
    val url: String
)

enum class StageName {
    EGG,
    BABY,
    CHILD,
    TEEN,
    ADULT
}