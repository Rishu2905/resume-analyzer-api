package com.axis.hraiportal.repository.supabase;

import com.axis.hraiportal.client.SupabaseClient;
import com.axis.hraiportal.domain.jd.JobDescriptionModel;
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
            String hrId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "hr_id=eq." + hrId,
                JobDescriptionModel.class);
    }

    // ── Delete JD ────────────────────────────────────────────
    public Mono<Void> deleteByJdId(String jdId) {
        return supabaseClient.delete(
                TABLE,
                "jd_id=eq." + jdId);
    }
}