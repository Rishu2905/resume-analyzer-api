package com.axis.hraiportal.modules.session.service;

import com.axis.hraiportal.modules.document.repository.DocumentRepository;
import com.axis.hraiportal.modules.session.dtoresponse.rankingResponse;
import com.axis.hraiportal.modules.session.entity.SessionModel;
import com.axis.hraiportal.modules.session.repository.SessionRepository;
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

    // ─────────────────────────────────────────────
    // CREATE NEW SESSION
    // ─────────────────────────────────────────────
    public Mono<SessionModel> createSession(
            String userId,
            String title) {

        SessionModel session = SessionModel.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        return sessionRepository.save(session)
                .doOnSuccess(s -> log.info(
                        "Session created: {} for HR: {}",
                        s.getSessionId(),
                        userId));
    }


    // ─────────────────────────────────────────────
    // GET ALL SESSIONS OF HR
    // ─────────────────────────────────────────────
    public Mono<List<SessionModel>> getSessionsByHr(
            String userId) {

        return sessionRepository.findByHrId(userId);
    }


    // ─────────────────────────────────────────────
    // GET SINGLE SESSION
    // ─────────────────────────────────────────────
    public Mono<SessionModel> getById(
            String userId,
            String sessionId) {

        // first get all sessions of this HR
        return sessionRepository

                .findByHrIdAndSessionId(userId,sessionId)

                .flatMap(list -> {

                    // no sessions for this HR
                    if (list.isEmpty()) {

                        return Mono.error(
                                new RuntimeException(
                                        "No sessions found for HR"));
                    }

                    // find matching session inside HR-owned sessions
                    return list.stream()

                            .filter(session ->
                                    session.getSessionId()
                                            .equals(sessionId))

                            .findFirst()

                            .map(Mono::just)

                            .orElseGet(() ->
                                    Mono.error(
                                            new RuntimeException(
                                                    "Session not found or access denied")));
                });
    }


    // ─────────────────────────────────────────────
    // UPDATE SESSION TITLE
    // ─────────────────────────────────────────────
    public Mono<SessionModel> updateTitle(
            String sessionId,
            String userId,
            String newTitle) {

        return getById(sessionId, userId)

                .flatMap(existing -> {

                    existing.setTitle(newTitle);

                    existing.setUpdatedAt(
                            LocalDateTime.now());

                    return sessionRepository
                            .updateTitle(
                                    sessionId,
                                    existing);
                });
    }


    // ─────────────────────────────────────────────
    // DELETE SESSION
    // ─────────────────────────────────────────────
    public Mono<Void> deleteSession(
            String sessionId,
            String userId) {

        return getById(sessionId, userId)

                .flatMap(existing ->
                        sessionRepository
                                .deleteBySessionId(
                                        sessionId))

                .doOnSuccess(v -> log.info(
                        "Session deleted: {}",
                        sessionId));
    }


    // ─────────────────────────────────────────────
    // GET SESSION RANKINGS
    // ─────────────────────────────────────────────
    public Mono<rankingResponse> getRanking(
            String sessionId,
            String userId) {

        // validate ownership first
        return getById(sessionId, userId)

                .flatMap(session ->
                        documentRepository
                                .findBySessionId(sessionId,userId))

                .map(documents -> {

                    var ranked = documents.stream()

                            .sorted(
                                    Comparator.comparingInt(
                                            d -> -d.getScore()))

                            .map(d ->
                                    rankingResponse
                                            .RankedResume
                                            .builder()
                                            .documentId(
                                                    d.getDocumentId())
                                            .filename(
                                                    d.getFilename())
                                            .mongoId(
                                                    d.getMongoId())
                                            .score(
                                                    d.getScore())
                                            .recommendation(
                                                    d.getRecommendation())
                                            .build())

                            .collect(Collectors.toList());

                    String jobTitle =
                            documents.isEmpty()
                                    ? "N/A"
                                    : documents
                                      .getFirst()
                                      .getJobTitle();

                    return rankingResponse.builder()

                            .sessionId(sessionId)

                            .jobTitle(jobTitle)

                            .totalResumes(
                                    documents.size())

                            .rankings(ranked)

                            .build();
                });
    }
}
