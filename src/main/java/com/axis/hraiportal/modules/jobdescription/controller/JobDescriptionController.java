package com.axis.hraiportal.modules.jobdescription.controller;

import com.axis.hraiportal.modules.jobdescription.entity.JobDescriptionModel;
import com.axis.hraiportal.modules.jobdescription.service.JobDescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/jd")
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionController {

    private final JobDescriptionService jdService;

    // ─────────────────────────────────────────────
// CREATE JOB DESCRIPTION
// POST /api/jd
// ─────────────────────────────────────────────
    @PostMapping
    public Mono<ResponseEntity<JobDescriptionModel>> createJd(

            @Valid @RequestBody JobDescriptionModel request) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        jdService.createJd(

                                userId,

                                request.getSessionId(),

                                request.getTitle(),

                                request.getDescription()))

                .map(ResponseEntity::ok);
    }

    // ─────────────────────────────────────────────
// GET JD BY SESSION
// GET /api/jd/session/{sessionId}
// ─────────────────────────────────────────────
    @GetMapping("/session/{sessionId}")
    public Mono<ResponseEntity<List<JobDescriptionModel>>>
    getBySession(

            @PathVariable String sessionId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        jdService.getBySession(
                                userId,
                                sessionId))

                .map(ResponseEntity::ok);
    }

    // ─────────────────────────────────────────────
// UPDATE JD
// PATCH /api/jd/{jdId}
// ─────────────────────────────────────────────
    @PatchMapping("/{jdId}")
    public Mono<ResponseEntity<JobDescriptionModel>> updateJd(

            @PathVariable String jdId,

            @RequestBody JobDescriptionModel request) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        jdService.updateJd(

                                userId,

                                jdId,

                                request.getTitle(),

                                request.getDescription()))

                .map(ResponseEntity::ok);
    }



    // ─────────────────────────────────────────────
// DELETE JD
// DELETE /api/jd/{jdId}
// ─────────────────────────────────────────────
    @DeleteMapping("/{jdId}")
    public Mono<ResponseEntity<Void>> deleteJd(

            @PathVariable String jdId) {

        return ReactiveSecurityContextHolder
                .getContext()

                .map(context ->
                        context.getAuthentication()
                                .getPrincipal()
                                .toString())

                .flatMap(userId ->
                        jdService.deleteJd(
                                userId,
                                jdId))

                .then(Mono.just(
                        ResponseEntity
                                .<Void>noContent()
                                .build()));
    }
}
