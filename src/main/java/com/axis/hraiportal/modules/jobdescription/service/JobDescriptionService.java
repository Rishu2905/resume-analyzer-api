package com.axis.hraiportal.modules.jobdescription.service;

import com.axis.hraiportal.modules.jobdescription.entity.JobDescriptionModel;
import com.axis.hraiportal.modules.jobdescription.repository.JobDescriptionRepository;
import com.axis.hraiportal.modules.session.service.SessionService;
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
public class JobDescriptionService {

    private final JobDescriptionRepository jdRepository;
    private final SessionService sessionService;

    // ─────────────────────────────────────────────
// CREATE NEW JD
// ─────────────────────────────────────────────
    public Mono<JobDescriptionModel> createJd(

            String userId,

            String sessionId,

            String title,

            String description) {

        // validate session ownership first
        return sessionService

                .getById(
                        userId,
                        sessionId)

                .flatMap(session -> {

                    JobDescriptionModel jd =
                            JobDescriptionModel.builder()

                                    .jdId(
                                            UUID.randomUUID()
                                                    .toString())

                                    .sessionId(sessionId)

                                    .userId(userId)

                                    .title(title)

                                    .description(description)

                                    .createdAt(
                                            LocalDateTime.now())
                                    .deleted(false)

                                    .build();

                    return jdRepository.save(jd);
                })

                .doOnSuccess(j -> log.info(
                        "JD created: {} in session: {}",
                        j.getJdId(),
                        sessionId));
    }

    // ─────────────────────────────────────────────
// GET JD BY SESSION
// ─────────────────────────────────────────────
    public Mono<List<JobDescriptionModel>> getBySession(

            String userId,

            String sessionId) {

        // validate ownership first
        return sessionService

                .getById(
                        userId,
                        sessionId)

                .flatMap(session ->
                        jdRepository
                                .findBySessionId(sessionId));
    }

    // ── Get single JD ───────────────────────────────────────
    public Mono<JobDescriptionModel> getById(String jdId) {
        return jdRepository.findByJdId(jdId)
                .flatMap(list -> list.isEmpty()
                        ? Mono.error(new RuntimeException(
                        "JD not found: " + jdId))
                        : Mono.just(list.get(0)));
    }

    // ─────────────────────────────────────────────
// UPDATE JD
// ─────────────────────────────────────────────
    public Mono<JobDescriptionModel> updateJd(

            String userId,

            String jdId,

            String title,

            String description) {

        return jdRepository

                .findByJdId(jdId)

                .flatMap(list -> {

                    if (list.isEmpty()) {

                        return Mono.error(
                                new RuntimeException(
                                        "JD not found"));
                    }

                    JobDescriptionModel jd =
                            list.getFirst();

                    // ownership validation
                    if (!jd.getUserId().equals(userId)) {

                        return Mono.error(
                                new RuntimeException(
                                        "Access denied"));
                    }

                    jd.setTitle(title);

                    jd.setDescription(description);

                    return jdRepository.updateJd(
                            jdId,
                            jd);
                });
    }



    // ─────────────────────────────────────────────
// DELETE JD
// ─────────────────────────────────────────────
    public Mono<Void> deleteJd(

            String userId,

            String jdId) {

        return jdRepository

                .findByJdId(jdId)

                .flatMap(list -> {

                    if (list.isEmpty()) {

                        return Mono.error(
                                new RuntimeException(
                                        "JD not found"));
                    }

                    JobDescriptionModel jd =
                            list.getFirst();

                    // ownership validation
                    if (!jd.getUserId().equals(userId)) {

                        return Mono.error(
                                new RuntimeException(
                                        "Access denied"));
                    }

                    return jdRepository.deleteByJdId(
                            jdId);
                });
    }
}
