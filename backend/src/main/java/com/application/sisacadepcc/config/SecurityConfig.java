package com.application.sisacadepcc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/", "/api/auth/*", "/login", "/oauth2/", "/logout*", "/error").permitAll()

                        // Endpoint específico para que estudiantes vean sus cursos
                        .requestMatchers("/api/students/my-courses").authenticated()

                        // Endpoints de usuario (accesibles para todos autenticados)
                        .requestMatchers("/api/user/me").authenticated()

                        // Endpoints de administración (solo administradores)
                        .requestMatchers(
                                "/api/classrooms/**",
                                "/api/courses/**",
                                "/api/professors/**",
                                "/api/secretaries/**",
                                "/api/students/**", // Esto incluye todos los endpoints de students excepto my-courses
                                "/api/administrators/**"
                        ).authenticated() // La autorización específica se maneja en los controladores

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:5173/bienvenido", true)
                        .failureUrl("http://localhost:5173/?error=auth_failed")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("http://localhost:5173/?logout=success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}