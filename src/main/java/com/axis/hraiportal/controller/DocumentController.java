package com.axis.hraiportal.controller;

import com.axis.hraiportal.domain.resume.DocumentRecord;
import com.axis.hraiportal.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    // POST /api/documents/upload
    @PostMapping("/upload")
    public Mono<ResponseEntity<DocumentRecord>> uploadResume(
            @RequestBody DocumentRecord request) {
        return documentService
                .uploadResume(
                        request.getSessionId(),
                        request.getHrId(),
                        request.getFilename(),
                        request.getFileHash(),
                        "raw text placeholder",  // PDF parsing comes later
                        List.of())
                .map(ResponseEntity::ok);
    }

    // GET /api/documents/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<List<DocumentRecord>>>
    getBySession(@PathVariable String sessionId) {
        return documentService.getBySession(sessionId)
                .map(ResponseEntity::ok);
    }
}