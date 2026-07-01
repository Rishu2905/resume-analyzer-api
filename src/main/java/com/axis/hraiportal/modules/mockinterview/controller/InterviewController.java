package com.axis.hraiportal.modules.mockinterview.controller;

import com.axis.hraiportal.modules.mockinterview.dto.InterviewResponseDTO;
import com.axis.hraiportal.modules.mockinterview.dto.MessageRequestDTO;
import com.axis.hraiportal.modules.mockinterview.dto.VerdictResponseDTO;
import com.axis.hraiportal.modules.mockinterview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/candidate/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Mono<InterviewResponseDTO> startInterview(
            @AuthenticationPrincipal String userId) {
        return interviewService.startInterview(userId);
    }

    @PostMapping("/{interviewId}/message")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Mono<InterviewResponseDTO> sendMessage(
            @PathVariable String interviewId,
            @RequestBody MessageRequestDTO request) {
        return interviewService.sendMessage(
                interviewId,
                request.getContent()
        );
    }

    @GetMapping("/{interviewId}/verdict")
    @PreAuthorize("hasRole('CANDIDATE')")
    public Mono<VerdictResponseDTO> getVerdict(
            @PathVariable String interviewId) {
        return interviewService.getVerdict(interviewId);
    }
}