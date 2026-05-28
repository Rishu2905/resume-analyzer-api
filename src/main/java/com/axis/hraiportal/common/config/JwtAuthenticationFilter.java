package com.axis.hraiportal.common.config;

import com.axis.hraiportal.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        // Step 1 — get Authorization header
        String header = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // Step 2 — no token → pass through
        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Step 3 — extract token
        String token = header.substring(7);

        // Step 4 — validate token
        if (!jwtUtil.isValid(token)) {
            log.warn("Invalid JWT on: {}",
                    exchange.getRequest().getPath());
            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Step 5 — extract hrId
        String hrId = jwtUtil.extractHrId(token);

        // Step 6 — create authentication
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        hrId, null, Collections.emptyList());

        // Step 7 — set in reactive security context
        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder
                                .withAuthentication(auth));
    }
}