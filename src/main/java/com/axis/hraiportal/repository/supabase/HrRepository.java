package com.axis.hraiportal.repository.supabase;

import com.axis.hraiportal.client.SupabaseClient;
import com.axis.hraiportal.domain.hr.HrUserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HrRepository {

    private final SupabaseClient supabaseClient;

    private static final String TABLE = "hrs";

    // ── Register a new HR user ───────────────────────────────
    public Mono<HrUserModel> save(HrUserModel hrUser) {
        return supabaseClient
                .insert(TABLE, hrUser, HrUserModel.class)
                .doOnSuccess(h -> log.info(
                        "Registered HR user: {}", h.getEmail()));
    }

    // ── Find HR user by ID ───────────────────────────────────
    public Mono<List<HrUserModel>> findByHrId(String hrId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "hr_id=eq." + hrId,
                HrUserModel.class);
    }

    // ── Find HR user by email (used for login) ───────────────
    public Mono<List<HrUserModel>> findByEmail(String email) {
        return supabaseClient.getWithFilter(
                TABLE,
                "email=eq." + email,
                HrUserModel.class);
    }

    // ── Update HR user ───────────────────────────────────────
    public Mono<HrUserModel> update(
            String hrId, HrUserModel updated) {
        return supabaseClient.update(
                TABLE,
                "hr_id=eq." + hrId,
                updated,
                HrUserModel.class);
    }
}