package com.axis.hraiportal.common.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.axis.hraiportal.common.exception.SupabaseException;

import java.util.List;

@Component
@Slf4j
public class SupabaseClient {

    private final WebClient publicClient;
    private final WebClient adminClient;

    public SupabaseClient(
            @Qualifier("supabasePublicClient") WebClient publicClient,
            @Qualifier("supabaseAdminClient") WebClient adminClient) {
        this.publicClient = publicClient;
        this.adminClient = adminClient;
    }

    // ── GET all rows from a table ────────────────────────────
    public <T> Mono<List<T>> getAll(String table, Class<T> type) {
        return publicClient.get()
                .uri("/rest/v1/" + table)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new SupabaseException(
                                        res.statusCode(), body)))
                .bodyToFlux(type)
                .collectList()
                .doOnError(e -> log.error(
                        "Supabase GET {} failed: {}", table, e.getMessage()));
    }

    // ── GET with PostgREST filter ────────────────────────────
    // filterQuery example: "id=eq.123&status=eq.active"
    public <T> Mono<List<T>> getWithFilter(
            String table, String filterQuery, Class<T> type) {
        return publicClient.get()
                .uri(uri -> uri
                        .path("/rest/v1/" + table)
                        .query(filterQuery)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new SupabaseException(
                                        res.statusCode(), body)))
                .bodyToFlux(type)
                .collectList()
                .doOnError(e -> log.error(
                        "Supabase GET {} filter={} failed: {}",
                        table, filterQuery, e.getMessage()));
    }

    // ── INSERT a row ─────────────────────────────────────────
    public <T> Mono<T> insert(
            String table, Object body, Class<T> responseType) {
        return adminClient.post()
                .uri("/rest/v1/" + table)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(b -> new SupabaseException(
                                        res.statusCode(), b)))
                .bodyToFlux(responseType)   // ← changed from bodyToMono to bodyToFlux
                .next()                     // ← takes first element from array
                .doOnError(e -> log.error(
                        "Supabase INSERT {} failed: {}",
                        table, e.getMessage()));
    }

    // ── UPDATE rows matching filter ──────────────────────────
    public <T> Mono<T> update(
            String table, String filterQuery,
            Object body, Class<T> responseType) {
        return adminClient.patch()
                .uri("/rest/v1/" + table + "?" + filterQuery)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(b -> new SupabaseException(
                                        res.statusCode(), b)))
                .bodyToMono(responseType)
                .doOnError(e -> log.error(
                        "Supabase UPDATE {} failed: {}", table, e.getMessage()));
    }

    // ── DELETE rows matching filter ──────────────────────────
    public Mono<Void> delete(String table, String filterQuery) {
        return adminClient.delete()
                .uri("/rest/v1/" + table + "?" + filterQuery)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(b -> new SupabaseException(
                                        res.statusCode(), b)))
                .bodyToMono(Void.class)
                .doOnError(e -> log.error(
                        "Supabase DELETE {} failed: {}", table, e.getMessage()));
    }
}
