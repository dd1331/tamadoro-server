package com.hobos.tamadoro.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web configuration for the application.
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    
    /**
     * Configure CORS (Cross-Origin Resource Sharing) to allow requests from the frontend.
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*") // In production, this should be restricted to specific origins
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600)
    }
}