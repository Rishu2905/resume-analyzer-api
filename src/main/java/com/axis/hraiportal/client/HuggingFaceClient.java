package com.axis.hraiportal.client;

import com.axis.hraiportal.properties.HuggingFaceProperties;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class HuggingFaceClient {

    private final WebClient huggingFaceWebClient;
    private final HuggingFaceProperties props;

    public HuggingFaceClient(
            @Qualifier("huggingFaceWebClient") WebClient huggingFaceWebClient,
            HuggingFaceProperties props) {
        this.huggingFaceWebClient = huggingFaceWebClient;
        this.props = props;
    }

    // ── Generate embedding vector for text ───────────────────
    // Returns float[768] — feeds directly into Pinecone upsert
    @CircuitBreaker(name = "huggingface")
    public Mono<List<Float>> embed(String text) {
        return huggingFaceWebClient.post()
                .uri("/pipeline/feature-extraction/"
                        + props.getEmbeddingModel())
                .bodyValue(Map.of("inputs", text))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(b -> new RuntimeException(
                                        "HuggingFace embed failed: " + b)))
                .bodyToMono(JsonNode.class)
                .map(this::parseEmbedding)
                .doOnSuccess(v -> log.debug(
                        "Embedding generated, dim={}", v.size()))
                .doOnError(e -> log.error(
                        "HuggingFace embed error: {}", e.getMessage()));
    }

    // HuggingFace returns [[...values...]] — extract first element
    private List<Float> parseEmbedding(JsonNode node) {
        List<Float> result = new ArrayList<>();
        JsonNode vector = node.isArray() ? node.get(0) : node;
        vector.forEach(v -> result.add((float) v.asDouble()));
        return result;
    }
}