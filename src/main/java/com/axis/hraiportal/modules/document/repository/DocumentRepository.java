package com.axis.hraiportal.modules.document.repository;

import com.axis.hraiportal.common.client.SupabaseClient;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DocumentRepository {

    private final SupabaseClient supabaseClient;

    private static final String TABLE = "documents";

    // ── Save a new document record ───────────────────────────
    public Mono<DocumentRecord> save(DocumentRecord document) {
        return supabaseClient
                .insert(TABLE, document, DocumentRecord.class);
    }

    // ── Find by document ID ──────────────────────────────────
    public Mono<List<DocumentRecord>> findByDocumentId(String documentId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "document_id=eq." + documentId,
                DocumentRecord.class);
    }

    // ── Find all documents in a session ─────────────────────
    public Mono<List<DocumentRecord>> findBySessionId(String sessionId, String userId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "session_id=eq." + sessionId + "&user_id=eq." + userId,
                DocumentRecord.class);
    }

    // ── Find by mongo_id ─────────────────────────────────────
    public Mono<List<DocumentRecord>> findByMongoId(String mongoId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "mongo_id=eq." + mongoId,
                DocumentRecord.class);
    }

    // ── Find all documents uploaded by a user ────────────────
    public Mono<List<DocumentRecord>> findByUserId(String userId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "user_id=eq." + userId,
                DocumentRecord.class);
    }

    // ── Find latest document by userId ───────────────────────
    // used by InterviewService to get docId before starting interview
    public Mono<DocumentRecord> findLatestByUserId(String userId) {
        return supabaseClient.getWithFilter(
                        TABLE,
                        "user_id=eq." + userId +
                                "&is_deleted=eq.false" +
                                "&order=uploaded_at.desc" +
                                "&limit=1",
                        DocumentRecord.class)
                .mapNotNull(list -> list.isEmpty() ? null : list.get(0));
    }

    // ── Soft delete ──────────────────────────────────────────
    public Mono<Void> deleteByDocumentId(String documentId) {
        return supabaseClient.delete(
                TABLE,
                "document_id=eq." + documentId);
    }
}