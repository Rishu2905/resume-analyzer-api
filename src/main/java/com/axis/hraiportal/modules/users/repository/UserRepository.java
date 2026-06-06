package com.axis.hraiportal.modules.users.repository;

import com.axis.hraiportal.common.client.SupabaseClient;
import com.axis.hraiportal.modules.users.entity.UserModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository {

    private final SupabaseClient supabaseClient;

    private static final String TABLE = "users";

    // ── Register a new HR user ───────────────────────────────
    public Mono<UserModel> save(UserModel hrUser) {
        return supabaseClient
                .insert(TABLE, hrUser, UserModel.class)
                .doOnSuccess(h -> log.info(
                        "Registered HR user: {}", h.getEmail()));
    }

    // ── Find HR user by ID ───────────────────────────────────
    public Mono<List<UserModel>> findByHrId(String userId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "user_id=eq." + userId,
                UserModel.class);
    }

    // ── Find HR user by email (used for login) ───────────────
    public Mono<List<UserModel>> findByEmail(String email) {
        return supabaseClient.getWithFilter(
                TABLE,
                "email=eq." + email,
                UserModel.class);
    }

    // ── Update HR user ───────────────────────────────────────
    public Mono<UserModel> update(
            String userId, UserModel updated) {
        return supabaseClient.update(
                TABLE,
                "user_id=eq." + userId,
                updated,
                UserModel.class);
    }
}
