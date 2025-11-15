// test/fixtures/BackgroundFixture.kt
package com.hobos.tamadoro.test.fixtures

import com.hobos.tamadoro.domain.tamas.entity.BackgroundEntity

object BackgroundFixture {
    fun create(
        title: String = "default title",
        theme: String = "default theme",
        url: String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTY1W-2yntdLMKaGe1BCTMS8q_WmW0Htigl55wVwwXjKQ&s=10",
    ): BackgroundEntity =
        BackgroundEntity(
            title = title,
            theme = theme,
            url = url,
            userId = null
        )
}