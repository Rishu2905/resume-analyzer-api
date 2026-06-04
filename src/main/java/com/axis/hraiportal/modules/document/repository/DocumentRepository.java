package com.axis.hraiportal.modules.document.repository;

import com.axis.hraiportal.common.client.SupabaseClient;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import com.axis.hraiportal.modules.jobdescription.entity.JobDescriptionModel;
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
                .insert(TABLE, document, DocumentRecord.class)
                .doOnSuccess(d -> log.debug(
                        "Saved document: {}", d.getDocumentId()));            // debug log
    }

    // ── Find by document ID ──────────────────────────────────
    public Mono<List<DocumentRecord>> findByDocumentId(
            String documentId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "document_id=eq." + documentId,
                DocumentRecord.class);
    }

    // ── Find all documents in a session ─────────────────────
    public Mono<List<DocumentRecord>> findBySessionId(
            String sessionId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "session_id=eq." + sessionId,
                DocumentRecord.class);
    }

    // ── Find by mongo_id ─────────────────────────────────────
    // Used to link Supabase record back to MongoDB document
    public Mono<List<DocumentRecord>> findByMongoId(
            String mongoId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "mongo_id=eq." + mongoId,
                DocumentRecord.class);
    }

    // ── Find all documents uploaded by an HR user ────────────
    public Mono<List<DocumentRecord>> findByUserId(String userId) {
        return supabaseClient.getWithFilter(
                TABLE,
                "hr_id=eq." + userId,
                DocumentRecord.class);
    }
    // update this method, instead of deleting set is_deleted=True
    // ── Delete a document record ─────────────────────────────
    public Mono<Void> deleteByDocumentId(String documentId) {
        return supabaseClient.delete(
                TABLE,
                "document_id=eq." + documentId);
    }

}
