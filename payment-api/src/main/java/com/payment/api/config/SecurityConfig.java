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
 * WARNING: This is TEMPORARY permissive config for development
 * All endpoints are currently OPEN - need to add JWT auth before production!
 * 
 * TODO (HIGH PRIORITY):
 * 1. Implement JwtAuthenticationFilter
 * 2. Create UserDetailsService 
 * 3. Lock down /api/v1/payments/** endpoints
 * 4. Add role-based authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // disabled for REST API with JWT
            
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // public endpoints
                .requestMatchers(
                    "/api/v1/payments/health",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // FIXME: lock these down after JWT is implemented
                // actuator should require auth in prod
                // .requestMatchers("/actuator/**").authenticated()
                
                // payment endpoints - currently OPEN for testing
                // this is a HUGE security hole, fix ASAP
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
