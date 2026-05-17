package com.axis.hraiportal.modules.session.service;

import com.axis.hraiportal.modules.document.repository.DocumentRepository;
import com.axis.hraiportal.modules.session.dtoresponse.rankingResponse;
import com.axis.hraiportal.modules.session.entity.SessionModel;
import com.axis.hraiportal.modules.session.repository.SessionRepository;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;
    private final DocumentRepository documentRepository;

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

    // getRanking function
    public Mono<rankingResponse> getRanking(String sessionId){
        return documentRepository.findBySessionId(sessionId)
                .map(documents->{
                    var ranked = documents.stream()
                            .sorted(Comparator.comparingInt(
                                    d -> -d.getScore()))
                            .map(d ->
                                    rankingResponse.RankedResume
                                            .builder()
                                            .documentId(d.getDocumentId())
                                            .filename(d.getFilename())
                                            .mongoId(d.getMongoId())
                                            .score(d.getScore())
                                            .recommendation(d.getRecommendation())
                                            .build())
                            .collect(Collectors.toList());

                    String jobTitle = documents.isEmpty()
                            ? "N/A"
                            : documents.getFirst().getJobTitle();
                    return rankingResponse.builder()                   //for building final API response
                            .sessionId(sessionId)
                            .jobTitle(jobTitle)
                            .totalResumes(documents.size())
                            .rankings(ranked)
                            .build();
                });
    }
}
