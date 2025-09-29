// test/fixtures/BackgroundFixture.kt
package com.hobos.tamadoro.test.fixtures

import com.hobos.tamadoro.domain.collections.BackgroundEntity

object BackgroundFixture {
    fun create(
        title: String = "default title",
        theme: String = "default theme",
        url: String = "https://example.com/bg.jpg",
    ): BackgroundEntity =
        BackgroundEntity(
            title = title,
            theme = theme,
            url = url,
        )
}