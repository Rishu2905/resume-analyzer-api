package com.axis.hraiportal.modules.jobdescription.service;

import com.axis.hraiportal.modules.jobdescription.entity.JobDescriptionModel;
import com.axis.hraiportal.modules.jobdescription.repository.JobDescriptionRepository;
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

    // ── Create a new JD ──────────────────────────────────────
    public Mono<JobDescriptionModel> createJd(
            String sessionId,
            String hrId,
            String title,
            String description) {

        JobDescriptionModel jd = JobDescriptionModel.builder()
                .jdId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .hrId(hrId)
                .title(title)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        return jdRepository.save(jd)
                .doOnSuccess(j -> log.info(
                        "JD created: {} in session: {}",
                        j.getJdId(), sessionId));
    }

    // ── Get all JDs in a session ─────────────────────────────
    public Mono<List<JobDescriptionModel>> getBySession(
            String sessionId) {
        return jdRepository.findBySessionId(sessionId);
    }

    // ── Get single JD ───────────────────────────────────────
    public Mono<JobDescriptionModel> getById(String jdId) {
        return jdRepository.findByJdId(jdId)
                .flatMap(list -> list.isEmpty()
                        ? Mono.error(new RuntimeException(
                        "JD not found: " + jdId))
                        : Mono.just(list.get(0)));
    }

    // ── Delete JD ────────────────────────────────────────────
    public Mono<Void> deleteJd(String jdId) {
        return jdRepository.deleteByJdId(jdId)
                .doOnSuccess(v -> log.info(
                        "JD deleted: {}", jdId));
    }
}
