package com.sivalab.laboperations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP Security with RBAC
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS Configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF Protection - Disabled for API endpoints
            .csrf(AbstractHttpConfigurer::disable)

            // Authorization Rules with Role-Based Access Control
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/login", "/login.html", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // H2 console for development
                .requestMatchers("/actuator/health").permitAll() // Health check

                // Admin-only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/equipment/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/api/v1/inventory/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/api/v1/monitoring/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/workflow/**").hasRole("ADMIN")

                // Reception endpoints
                .requestMatchers("/reception/**").hasAnyRole("ADMIN", "RECEPTION")
                .requestMatchers("/visits/**").hasAnyRole("ADMIN", "RECEPTION", "PHLEBOTOMIST", "TECHNICIAN")
                .requestMatchers("/billing/**").hasAnyRole("ADMIN", "RECEPTION")

                // Phlebotomy endpoints
                .requestMatchers("/phlebotomy/**").hasAnyRole("ADMIN", "PHLEBOTOMIST")
                .requestMatchers("/samples/**").hasAnyRole("ADMIN", "PHLEBOTOMIST", "TECHNICIAN")
                .requestMatchers("/sample-collection/**").hasAnyRole("ADMIN", "PHLEBOTOMIST", "TECHNICIAN")
                .requestMatchers("/test-templates/**").hasAnyRole("ADMIN", "RECEPTION", "PHLEBOTOMIST", "TECHNICIAN")

                // Technician endpoints
                .requestMatchers("/technician/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/api/v1/tests/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/api/v1/reports/**").hasAnyRole("ADMIN", "TECHNICIAN")

                // History and audit endpoints
                .requestMatchers("/samples/**").hasAnyRole("ADMIN", "PHLEBOTOMIST", "TECHNICIAN")
                .requestMatchers("/lab-tests/**").hasAnyRole("ADMIN", "TECHNICIAN")
                .requestMatchers("/audit-trail/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Form-based login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Security Headers Configuration
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )

            // Session Management
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    /**
     * Password encoder for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-memory user details service for development
     * In production, this should be replaced with database-backed user service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails reception = User.builder()
                .username("reception")
                .password(passwordEncoder().encode("reception123"))
                .roles("RECEPTION")
                .build();

        UserDetails phlebotomist = User.builder()
                .username("phlebotomy")
                .password(passwordEncoder().encode("phlebotomy123"))
                .roles("PHLEBOTOMIST")
                .build();

        UserDetails technician = User.builder()
                .username("technician")
                .password(passwordEncoder().encode("technician123"))
                .roles("TECHNICIAN")
                .build();

        return new InMemoryUserDetailsManager(admin, reception, phlebotomist, technician);
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
