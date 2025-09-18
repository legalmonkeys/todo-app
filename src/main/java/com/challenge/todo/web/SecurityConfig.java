package com.challenge.todo.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the todo Challenge application.
 * Provides essential security headers and CORS configuration for local development.
 * 
 * This configuration prioritizes simplicity and security for a development/demo environment
 * while maintaining the ability to be extended for production use.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Main security filter chain configuration.
     * Configures security headers, CORS, and basic protections.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints (typical for REST APIs)
            .csrf(csrf -> csrf.disable())
            
            // Configure security headers
            .headers(headers -> headers
                // Content Security Policy - prevents XSS attacks
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"))
                
                // X-Frame-Options - prevents clickjacking (allow same origin for H2)
                .frameOptions(frame -> frame.sameOrigin())
                
                // X-Content-Type-Options - prevents MIME sniffing
                .contentTypeOptions(opt -> {})
                
                // HSTS - forces HTTPS in production
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true)
                    .preload(true))
                
                // Referrer Policy - controls referrer information
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                
                // Permissions Policy - controls browser features
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Permissions-Policy", 
                        "geolocation=(), microphone=(), camera=(), " +
                        "payment=(), usb=(), magnetometer=(), gyroscope=()");
                })
            )
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization (permit all for simplicity)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                .requestMatchers("/api/**").permitAll()        // Allow API access
                .requestMatchers("/lists/**").permitAll()      // Allow view access
                .requestMatchers("/", "/health", "/actuator/**").permitAll() // Allow basic endpoints
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll() // Static resources
                .anyRequest().permitAll() // Allow everything else for development
            );

        return http.build();
    }

    /**
     * CORS configuration for cross-origin requests.
     * Configured for local development with sensible defaults.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins for development
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",    // Local development
            "http://127.0.0.1:*",   // Local development alternative
            "https://localhost:*"    // HTTPS local development
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Accept",
            "Accept-Language", 
            "Content-Language",
            "Content-Type",
            "Authorization",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        // Expose standard headers
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Length",
            "Content-Type",
            "Date",
            "Server"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Apply to API endpoints
        source.registerCorsConfiguration("/lists/**", configuration); // Apply to view endpoints
        
        return source;
    }
}
