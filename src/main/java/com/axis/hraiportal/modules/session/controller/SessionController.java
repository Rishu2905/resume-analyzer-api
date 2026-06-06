package com.axis.hraiportal.modules.session.controller;

import com.axis.hraiportal.modules.session.dtoresponse.rankingResponse;
import com.axis.hraiportal.modules.session.entity.SessionModel;
import com.axis.hraiportal.modules.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;


    // POST /api/sessions
    // ─────────────────────────────────────────────
// CREATE SESSION
// POST /api/sessions
// ─────────────────────────────────────────────
    @PostMapping
    public Mono<ResponseEntity<SessionModel>> createSession(
            @Valid @RequestBody SessionModel request) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        sessionService.createSession(
                                userId,
                                request.getTitle()))

                .map(ResponseEntity::ok);
    }


    // ─────────────────────────────────────────────
// GET ALL SESSIONS OF AUTHENTICATED HR
// GET /api/sessions/hr
// ─────────────────────────────────────────────
    @GetMapping("/my-sessions")
    public Mono<ResponseEntity<List<SessionModel>>> getByHr() {

        return ReactiveSecurityContextHolder
                .getContext()
                // context variable coming from JwtAuthenticationFilter file
                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(sessionService::getSessionsByHr)

                .map(ResponseEntity::ok);
    }


    // ─────────────────────────────────────────────
// GET SESSION BY ID
// GET /api/sessions/{sessionId}
// ─────────────────────────────────────────────
    @GetMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionModel>> getById(
            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(hrId ->
                        sessionService.getById(hrId,
                                sessionId))

                .map(ResponseEntity::ok);
    }


    // ─────────────────────────────────────────────
// UPDATE SESSION TITLE
// PATCH /api/sessions/{sessionId}
// ─────────────────────────────────────────────
    @PatchMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionModel>> updateTitle(
            @PathVariable String sessionId,
            @RequestBody SessionModel request) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        sessionService.updateTitle(
                                sessionId,
                                userId,
                                request.getTitle()))

                .map(ResponseEntity::ok);
    }


    // ─────────────────────────────────────────────
// DELETE SESSION
// DELETE /api/sessions/{sessionId}
// ─────────────────────────────────────────────
    @DeleteMapping("/{sessionId}")
    public Mono<ResponseEntity<Void>> deleteSession(
            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        sessionService.deleteSession(
                                sessionId,
                                userId))

                .then(Mono.just(
                        ResponseEntity
                                .<Void>noContent()
                                .build()));
    }


    // ─────────────────────────────────────────────
// GET SESSION RANKINGS
// GET /api/sessions/{sessionId}/rankings
// ─────────────────────────────────────────────
    @GetMapping("/{sessionId}/rankings")
    public Mono<ResponseEntity<rankingResponse>> getRanking(
            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        sessionService.getRanking(
                                sessionId,
                                userId))

                .map(ResponseEntity::ok);
    }
}
