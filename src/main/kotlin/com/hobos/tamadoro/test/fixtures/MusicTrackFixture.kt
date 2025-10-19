import com.hobos.tamadoro.domain.tamas.MusicTrackEntity

object MusicTrackFixtures {
    fun create(
        resource: String = "default-res",
        theme: String = "default-theme",
        url: String = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
        title: String = "기본 타이틀"
    ): MusicTrackEntity {
        return MusicTrackEntity(resource, theme, url, title)
    }
}