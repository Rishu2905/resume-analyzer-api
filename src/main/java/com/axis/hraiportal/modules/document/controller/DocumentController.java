package com.axis.hraiportal.modules.document.controller;

import com.axis.hraiportal.modules.document.dto.response.DocumentResponse;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import com.axis.hraiportal.modules.document.service.DocumentService;
import com.axis.hraiportal.modules.resume.entity.ResumeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<ResponseEntity<DocumentResponse>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("session_id") String sessionId,
            @RequestParam("hr_id") String hrId,
            @RequestParam("job_title") String jobTitle)
    {

        return documentService
                .uploadResume(sessionId, hrId, file,jobTitle)
                .map(ResponseEntity::ok);
    }

    // GET /api/documents/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<List<DocumentRecord>>>
    getBySession(@PathVariable String sessionId) {
        return documentService.getBySession(sessionId)
                .map(ResponseEntity::ok);
    }

    // GET /api/documents/{mongoId}/analysis
    @GetMapping("/{mongoId}/analysis")
    public ResponseEntity<ResumeDocument> getAnalysis(
            @PathVariable String mongoId) {
        return ResponseEntity.ok(
                documentService.getAnalysis(mongoId));
    }
}
