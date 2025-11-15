import com.hobos.tamadoro.domain.tamas.entity.TamaCatalogEntity

object TamaFixtures {
    fun list(count: Int = 3): List<TamaCatalogEntity> =
        (1..count).map {
            create(title = "타마$it", theme = "theme$it")
        }

    fun create(
        theme: String = "default-theme",
        title: String = "기본 타마",
        url: String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10",
    ): TamaCatalogEntity {
        return TamaCatalogEntity(
            theme = theme,
            title = title,
            url = url,
        )
    }
}