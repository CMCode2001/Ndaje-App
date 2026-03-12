package com.ndaje.trip.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité permissive pour les tests d'intégration.
 * Remplace la SecurityConfig principale (qui nécessite Keycloak) par une
 * version qui autorise toutes les requêtes sans validation JWT.
 *
 * Activée uniquement pour le profil "integration".
 */
@TestConfiguration
@EnableWebSecurity
@Profile("integration")
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }
}
