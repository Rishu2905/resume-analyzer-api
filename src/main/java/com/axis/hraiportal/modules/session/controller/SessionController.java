package com.axis.hraiportal.modules.session.controller;

import com.axis.hraiportal.modules.session.entity.SessionModel;
import com.axis.hraiportal.modules.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;

    // POST /api/sessions
    @PostMapping
    public Mono<ResponseEntity<SessionModel>> createSession(
            @RequestBody SessionModel request) {
        return sessionService
                .createSession(
                        request.getHrId(),
                        request.getTitle())
                .map(ResponseEntity::ok);
    }

    // GET /api/sessions/hr/{hrId}
    @GetMapping("/hr/{hrId}")
    public Mono<ResponseEntity<List<SessionModel>>> getByHr(
            @PathVariable String hrId) {
        return sessionService.getSessionsByHr(hrId)
                .map(ResponseEntity::ok);
    }

    // GET /api/sessions/{sessionId}
    @GetMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionModel>> getById(
            @PathVariable String sessionId) {
        return sessionService.getById(sessionId)
                .map(ResponseEntity::ok);
    }

    // PATCH /api/sessions/{sessionId}
    @PatchMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionModel>> updateTitle(
            @PathVariable String sessionId,
            @RequestBody SessionModel request) {
        return sessionService
                .updateTitle(sessionId, request.getTitle())
                .map(ResponseEntity::ok);
    }

    // DELETE /api/sessions/{sessionId}
    @DeleteMapping("/{sessionId}")
    public Mono<ResponseEntity<Void>> deleteSession(
            @PathVariable String sessionId) {
        return sessionService.deleteSession(sessionId)
                .then(Mono.just(ResponseEntity
                        .<Void>noContent().build()));
    }
}
