package com.hobos.tamadoro.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "auth")
class AuthProperties {
    var secret: String = "change-me-base64"
    var accessTokenMinutes: Long = 60
    var refreshTokenDays: Long = 30
}
