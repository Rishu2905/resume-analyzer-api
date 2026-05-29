package com.axis.hraiportal.modules.session.repository;

import com.axis.hraiportal.common.client.SupabaseClient;
import com.axis.hraiportal.modules.session.entity.SessionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class  SessionRepository {

    private final SupabaseClient supabaseClient;

    private static final String TABLE = "sessions";

    // ── Create a new session ─────────────────────────────────
    public Mono<SessionModel> save(SessionModel session) {
        return supabaseClient
                .insert(TABLE, session, SessionModel.class)
                .doOnSuccess(s -> log.info(
                        "Created session: {}", s.getSessionId()));
    }

    // ── Find session by ID ───────────────────────────────────
    public Mono<List<SessionModel>> findBySessionId(
            String sessionId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "session_id=eq." + sessionId,
                SessionModel.class);
    }

    public Mono<List<SessionModel>> findByHrId(String hrId) {


        return supabaseClient.getWithFilter(
                        TABLE,
                        "hr_id=eq." + hrId,
                        SessionModel.class);
    }

    // ── Update session title ─────────────────────────────────
    public Mono<SessionModel> updateTitle(
            String sessionId, SessionModel updated) {
        return supabaseClient.update(
                TABLE,
                "session_id=eq." + sessionId,
                updated,
                SessionModel.class);
    }

    // ── Delete session ───────────────────────────────────────
    public Mono<Void> deleteBySessionId(String sessionId) {
        return supabaseClient.delete(
                TABLE,
                "session_id=eq." + sessionId);
    }
}
