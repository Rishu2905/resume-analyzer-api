package com.axis.hraiportal.modules.document.controller;

import com.axis.hraiportal.modules.document.dto.response.DocumentResponse;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import com.axis.hraiportal.modules.document.service.DocumentService;
import com.axis.hraiportal.modules.resume.entity.ResumeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    // POST /api/documents/upload
    // multipart/form-data — not JSON
    // ─────────────────────────────────────────────
// UPLOAD RESUME
// POST /api/documents/upload
// ─────────────────────────────────────────────
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<ResponseEntity<DocumentResponse>>
    uploadResume(

            @RequestPart("file")
            FilePart file,

            @RequestPart("session_id")
            String sessionId,

            @RequestPart("job_title")
            String jobTitle) {
//        log.debug("controller hit");

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        documentService.uploadResume(

                                userId,

                                sessionId,

                                file,

                                jobTitle))

                .map(ResponseEntity::ok);
    }

    // ─────────────────────────────────────────────
// GET DOCUMENTS BY SESSION
// GET /api/documents/session/{sessionId}
// ─────────────────────────────────────────────
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<List<DocumentRecord>>>
    getBySession(

            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(hrId ->
                        documentService.getBySession(
                                hrId,
                                sessionId))

                .map(ResponseEntity::ok);
    }

    // ─────────────────────────────────────────────
// GET SESSION ANALYSIS
// GET /api/documents/session/{sessionId}/analysis
// ─────────────────────────────────────────────
    @GetMapping("/session/{sessionId}/analysis")
    public Mono<ResponseEntity<List<ResumeDocument>>>
    getAnalysis(

            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(hrId ->
                        documentService.getAnalysis(
                                hrId,
                                sessionId))

                .map(ResponseEntity::ok);
    }
}
