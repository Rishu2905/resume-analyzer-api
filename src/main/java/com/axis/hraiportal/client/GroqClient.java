package com.axis.hraiportal.client;

import com.axis.hraiportal.properties.GroqProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GroqClient {

    private final WebClient groqWebClient;
    private final GroqProperties props;

    public GroqClient(
            @Qualifier("groqWebClient") WebClient groqWebClient,
            GroqProperties props) {
        this.groqWebClient = groqWebClient;
        this.props = props;
    }

    // ── Chat completion (OpenAI-compatible) ──────────────────
    @CircuitBreaker(name = "groq", fallbackMethod = "fallbackComplete")
    @Retry(name = "groq")
    public Mono<String> complete(
            String systemPrompt, String userPrompt) {

        var requestBody = Map.of(
                "model",       props.getModel(),
                "max_tokens",  props.getMaxTokens(),
                "temperature", props.getTemperature(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", userPrompt)
                )
        );

        return groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Groq error [" + res.statusCode() + "]: " + body)))
                .bodyToMono(JsonNode.class)
                .map(json -> json
                        .path("choices").get(0)
                        .path("message")
                        .path("content").asText())
                .doOnSuccess(r -> log.debug(
                        "Groq completion success, length={}", r.length()))                     // debug log
                .doOnError(e -> log.error(
                        "Groq completion failed: {}", e.getMessage()));
    }

    // ── Fallback when circuit is open ────────────────────────
    public Mono<String> fallbackComplete(
            String s, String u, Throwable t) {
        log.warn("Groq circuit open → fallback: {}", t.getMessage());
        return Mono.just(
                "AI service temporarily unavailable. Please try again shortly.");
    }
}