package com.axis.hraiportal.controller;

import com.axis.hraiportal.service.ResumeMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
public class ResumeMatchingController {

    private final ResumeMatchingService matchingService;

    // POST /api/match/{jdId}?topK=5
    @PostMapping("/{jdId}")
    public Mono<ResponseEntity<String>> matchResumes(
            @PathVariable String jdId,
            @RequestParam(defaultValue = "5") int topK) {
        return matchingService
                .matchResumesToJd(jdId, topK)
                .map(ResponseEntity::ok);
    }
}