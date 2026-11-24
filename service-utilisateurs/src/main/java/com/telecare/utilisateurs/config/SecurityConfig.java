package com.telecare.utilisateurs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                // Désactiver X-Frame-Options pour la console H2
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                .authorizeHttpRequests(authorize -> authorize

                        // ── Console H2 (dev) ───────────────────────────────────────────────
                        .requestMatchers("/h2-console/**").permitAll()

                        // ── Actuator (health/info exposés à l'API Gateway) ─────────────────
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ── Endpoints LIRE appelés par service-interventions via Feign ──────
                        // GET /api/utilisateurs/{id}       → vérification assistant (actif + rôle)
                        // GET /api/utilisateurs/{id}/verification → endpoint dédié
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/*/verification").permitAll()

                        // ── Tout le reste : authentification HTTP Basic requise ─────────────
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults())
                .formLogin(formLogin -> formLogin.disable())
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.security.username}") String username,
            @Value("${app.security.password}") String password,
            PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("ADMIN")
                        .build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
