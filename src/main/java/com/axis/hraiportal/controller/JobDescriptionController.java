package com.axis.hraiportal.controller;

import com.axis.hraiportal.domain.jd.JobDescriptionModel;
import com.axis.hraiportal.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/jd")
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionController {

    private final JobDescriptionService jdService;

    // POST /api/jd
    @PostMapping
    public Mono<ResponseEntity<JobDescriptionModel>> createJd(
            @RequestBody JobDescriptionModel request) {
        return jdService
                .createJd(
                        request.getSessionId(),
                        request.getHrId(),
                        request.getTitle(),
                        request.getDescription())
                .map(ResponseEntity::ok);
    }

    // GET /api/jd/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<List<JobDescriptionModel>>>
    getBySession(@PathVariable String sessionId) {
        return jdService.getBySession(sessionId)
                .map(ResponseEntity::ok);
    }

    // GET /api/jd/{jdId}
    @GetMapping("/{jdId}")
    public Mono<ResponseEntity<JobDescriptionModel>> getById(
            @PathVariable String jdId) {
        return jdService.getById(jdId)
                .map(ResponseEntity::ok);
    }

    // DELETE /api/jd/{jdId}
    @DeleteMapping("/{jdId}")
    public Mono<ResponseEntity<Void>> deleteJd(
            @PathVariable String jdId) {
        return jdService.deleteJd(jdId)
                .then(Mono.just(ResponseEntity
                        .<Void>noContent().build()));
    }
}