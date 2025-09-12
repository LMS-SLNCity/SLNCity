package com.sivalab.laboperations.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for Lab Operations Management System
 * 
 * Security Enhancement: Replaces insecure wildcard CORS origins with specific allowed origins.
 * This configuration provides secure cross-origin access while maintaining development flexibility.
 * 
 * @author Lab Operations Team
 * @version 1.0
 * @since 2025-09-12
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Secure origin patterns - no wildcard "*" for production security
                .allowedOriginPatterns(
                    "http://localhost:*",           // Local development (any port)
                    "http://127.0.0.1:*",          // Local development (any port)
                    "https://localhost:*",          // Local HTTPS development
                    "https://127.0.0.1:*",         // Local HTTPS development
                    "https://*.sivalab.com",        // Production domain pattern
                    "https://*.laboperations.com"   // Alternative production domain
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}
