package com.axis.hraiportal.modules.jobdescription.repository;

import com.axis.hraiportal.common.client.SupabaseClient;
import com.axis.hraiportal.modules.jobdescription.entity.JobDescriptionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionRepository {

    private final SupabaseClient supabaseClient;

    private static final String TABLE = "job_descriptions";

    // ── Save a new JD ────────────────────────────────────────
    public Mono<JobDescriptionModel> save(
            JobDescriptionModel jd) {
        return supabaseClient
                .insert(TABLE, jd, JobDescriptionModel.class)
                .doOnSuccess(j -> log.info(
                        "Saved JD: {}", j.getJdId()));
    }

    // ── Find JD by ID ────────────────────────────────────────
    public Mono<List<JobDescriptionModel>> findByJdId(
            String jdId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "jd_id=eq." + jdId,
                JobDescriptionModel.class);
    }

    // ── Find all JDs in a session ────────────────────────────
    public Mono<List<JobDescriptionModel>> findBySessionId(
            String sessionId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "session_id=eq." + sessionId,
                JobDescriptionModel.class);
    }

    // ── Find all JDs posted by an HR user ────────────────────
    public Mono<List<JobDescriptionModel>> findByHrId(
            String userId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "user_id=eq." + userId,
                JobDescriptionModel.class);
    }

    // ── Delete JD ────────────────────────────────────────────
    public Mono<Void> deleteByJdId(String jdId) {
        return supabaseClient.delete(
                TABLE,
                "jd_id=eq." + jdId);
    }

    // updateJd function
    public Mono<JobDescriptionModel> updateJd(String jdId,JobDescriptionModel jd){
        return supabaseClient.update(
                TABLE,"jd_id=eq." + jdId, jd,
                JobDescriptionModel.class);
    }

    // Authorization method
    // ─────────────────────────────────────────────
// FIND AUTHORIZED JD
// validates:
// 1. session belongs to HR
// 2. JD belongs to session
// ─────────────────────────────────────────────
    public Mono<JobDescriptionModel> findAuthorizedJd(

            String userId,

            String sessionId) {
//        log.debug("entered findAuthorisedJd fn in JDRepo");


        return supabaseClient


                .getWithFilter(

                        TABLE,
                        "session_id=eq." +sessionId+
                                "&user_id=eq." +userId,


                        JobDescriptionModel.class)


                .flatMap(list -> {
//                    log.debug(
//                            "Validating JD ownership: sessionId={}, userId={}",
//                            sessionId,
//                            userId);

                    if (list.isEmpty()) {

                        return Mono.error(
                                new RuntimeException(
                                        "Unauthorized session or JD not found"));
                    }

                    return Mono.just(
                            list.getFirst());
                });
    }
}
