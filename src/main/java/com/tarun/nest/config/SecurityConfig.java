package com.tarun.nest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access to auth endpoints and swagger/openapi resources
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        // NOTE: Do not enable HTTP Basic authentication here. Disabling httpBasic
        // prevents the browser from showing the basic auth popup when an endpoint
        // returns 401. Authentication is expected to be handled using JWT tokens
        // (see AuthService / JwtUtil). If you later need a custom auth entry point
        // or JWT filter, add it here.

        return http.build();
    }
}
