package com.payment.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration
 * 
 * NOTE: This is a PERMISSIVE configuration for initial setup
 * In production, you MUST:
 * 1. Implement proper JWT authentication
 * 2. Add role-based authorization
 * 3. Secure all endpoints appropriately
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API (using JWT)
            .csrf(csrf -> csrf.disable())
            
            // Stateless session management
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers(
                    "/api/v1/payments/health",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Actuator endpoints - require authentication (comment out temporarily)
                // .requestMatchers("/actuator/**").authenticated()
                
                // All payment endpoints - TEMPORARILY PERMIT ALL
                // TODO: Change to .authenticated() after implementing JWT
                .requestMatchers("/api/v1/payments/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Password encoder for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
