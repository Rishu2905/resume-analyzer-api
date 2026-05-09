package com.axis.hraiportal.service;

import com.axis.hraiportal.domain.session.SessionModel;
import com.axis.hraiportal.repository.supabase.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;

    // ── Create a new session ─────────────────────────────────
    public Mono<SessionModel> createSession(
            String hrId, String title) {

        SessionModel session = SessionModel.builder()
                .sessionId(UUID.randomUUID().toString())
                .hrId(hrId)
                .title(title)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return sessionRepository.save(session)
                .doOnSuccess(s -> log.info(
                        "Session created: {} for HR: {}",
                        s.getSessionId(), hrId));
    }

    // ── Get all sessions for an HR user ──────────────────────
    public Mono<List<SessionModel>> getSessionsByHr(
            String hrId) {
        return sessionRepository.findByHrId(hrId);
    }

    // ── Get single session ───────────────────────────────────
    public Mono<SessionModel> getById(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .flatMap(list -> list.isEmpty()
                        ? Mono.error(new RuntimeException(
                        "Session not found: " + sessionId))
                        : Mono.just(list.get(0)));
    }

    // ── Update session title ─────────────────────────────────
    public Mono<SessionModel> updateTitle(
            String sessionId, String newTitle) {

        return getById(sessionId)
                .flatMap(existing -> {
                    existing.setTitle(newTitle);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return sessionRepository.updateTitle(
                            sessionId, existing);
                });
    }

    // ── Delete session ───────────────────────────────────────
    public Mono<Void> deleteSession(String sessionId) {
        return sessionRepository.deleteBySessionId(sessionId)
                .doOnSuccess(v -> log.info(
                        "Session deleted: {}", sessionId));
    }
}