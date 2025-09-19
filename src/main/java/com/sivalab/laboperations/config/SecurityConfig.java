package com.sivalab.laboperations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Lab Operations System
 * Implements comprehensive security hardening with fault tolerance support
 */
import org.springframework.context.annotation.Profile;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    /**
     * Configure HTTP Security with comprehensive hardening
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF Protection - Disabled for API endpoints, enabled for web forms
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/**",
                    "/actuator/**",
                    "/h2-console/**"
                )
            )
            
            // Authorization Rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/actuator/**",           // Actuator endpoints for monitoring
                    "/api/v1/resilient/**",   // Resilient service endpoints
                    "/h2-console/**",         // H2 console for development
                    "/error",                 // Error pages
                    "/favicon.ico"            // Favicon
                ).permitAll()
                
                // API endpoints - require authentication
                .requestMatchers("/api/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // HTTP Basic Authentication for API access
            .httpBasic(basic -> basic.realmName("Lab Operations API"))
            
            // Security Headers Configuration
            .headers(headers -> headers
                // Frame Options - Allow same origin for H2 console
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            
            // Session Management
            .sessionManagement(session -> session
                .maximumSessions(10)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    /**
     * CORS Configuration for cross-origin requests
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure for production)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
