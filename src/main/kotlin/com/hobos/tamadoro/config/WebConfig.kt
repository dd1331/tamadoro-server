package com.hobos.tamadoro.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.beans.factory.annotation.Autowired

/**
 * Web configuration for the application.
 */
@Configuration
class WebConfig(
    @Autowired private val currentUserIdArgumentResolver: CurrentUserIdArgumentResolver
) : WebMvcConfigurer {
    
    /**
     * Configure CORS (Cross-Origin Resource Sharing) to allow requests from the frontend.
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentUserIdArgumentResolver)
    }
}