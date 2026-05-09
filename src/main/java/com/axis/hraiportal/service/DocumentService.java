package com.axis.hraiportal.service;

import com.axis.hraiportal.client.HuggingFaceClient;
import com.axis.hraiportal.client.PineconeClient;
import com.axis.hraiportal.domain.resume.DocumentRecord;
import com.axis.hraiportal.domain.resume.ResumeDocument;
import com.axis.hraiportal.repository.mongo.ResumeRepository;
import com.axis.hraiportal.repository.supabase.DocumentRepository;
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
public class DocumentService {

    private final ResumeRepository resumeRepository;
    private final DocumentRepository documentRepository;
    private final HuggingFaceClient huggingFaceClient;
    private final PineconeClient pineconeClient;

    // ── Full resume upload pipeline ──────────────────────────
    // 1. save raw text to MongoDB
    // 2. generate embedding via HuggingFace
    // 3. store embedding in Pinecone
    // 4. save document record to Supabase
    public Mono<DocumentRecord> uploadResume(
            String sessionId,
            String hrId,
            String filename,
            String fileHash,
            String rawText,
            List<String> skills) {

        // Step 1 — build and save MongoDB document
        ResumeDocument resumeDoc = ResumeDocument.builder()
                .filename(filename)
                .rawText(rawText)
                .skills(skills)
                .uploadedAt(LocalDateTime.now())
                .build();

        ResumeDocument savedDoc =
                resumeRepository.save(resumeDoc);
        String mongoId = savedDoc.getId();

        log.info("Resume saved to MongoDB: {}", mongoId);

        // Step 2 + 3 — embed and store in Pinecone
        return huggingFaceClient.embed(rawText)
                .flatMap(embedding -> {
                    pineconeClient.upsert(mongoId, embedding);
                    log.info("Embedding stored in Pinecone: {}",
                            mongoId);

                    // Step 4 — save metadata to Supabase
                    DocumentRecord record = DocumentRecord.builder()
                            .documentId(UUID.randomUUID().toString())
                            .sessionId(sessionId)
                            .hrId(hrId)
                            .filename(filename)
                            .fileHash(fileHash)
                            .mongoId(mongoId)
                            .uploadedAt(LocalDateTime.now())
                            .build();

                    return documentRepository.save(record);
                })
                .doOnSuccess(r -> log.info(
                        "Document upload complete: {}",
                        r.getDocumentId()))
                .doOnError(e -> log.error(
                        "Document upload failed: {}", e.getMessage()));
    }

    // ── Get all documents in a session ───────────────────────
    public Mono<List<DocumentRecord>> getBySession(
            String sessionId) {
        return documentRepository.findBySessionId(sessionId);
    }

    // ── Get MongoDB resume by mongo_id ───────────────────────
    public ResumeDocument getResumeByMongoId(String mongoId) {
        return resumeRepository.findById(mongoId)
                .orElseThrow(() -> new RuntimeException(
                        "Resume not found: " + mongoId));
    }
}