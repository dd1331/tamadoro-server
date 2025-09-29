import com.hobos.tamadoro.domain.collections.TamaCatalogEntity
import com.hobos.tamadoro.domain.collections.TamaCatalogStageEntity

object TamaFixtures {
    fun list(count: Int = 3): List<TamaCatalogEntity> =
        (1..count).map {
            create(title = "타마$it", theme = "theme$it")
        }

    fun create(
        theme: String = "default-theme",
        title: String = "기본 타마",
        url: String = "https://example.com/tama.png",
        stages: List<TamaCatalogStageEntity> = emptyList()
    ): TamaCatalogEntity {
        return TamaCatalogEntity(
            stages = stages,
            theme = theme,
            title = title,
            url = url
        )
    }
}