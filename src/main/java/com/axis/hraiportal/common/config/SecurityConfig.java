package com.axis.hraiportal.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityWebFilterChain filterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(CsrfSpec::disable)

                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/api/hr/register",
                                "/api/hr/login",
                                "/actuator/health"
                        ).permitAll()

                        .anyExchange()
                        .authenticated()
                )

                .addFilterAt(
                        jwtAuthFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )

                .httpBasic(
                        ServerHttpSecurity
                                .HttpBasicSpec::disable)

                .formLogin(
                        ServerHttpSecurity
                                .FormLoginSpec::disable)

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}