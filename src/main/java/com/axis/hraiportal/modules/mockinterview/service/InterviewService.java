package com.axis.hraiportal.modules.mockinterview.service;

import com.axis.hraiportal.modules.mockinterview.dto.InterviewResponseDTO;
import com.axis.hraiportal.modules.mockinterview.dto.VerdictResponseDTO;
import reactor.core.publisher.Mono;

public interface InterviewService {
    Mono<InterviewResponseDTO> startInterview(String userId);
    Mono<InterviewResponseDTO> sendMessage(String interviewId, String content);
    Mono<VerdictResponseDTO> getVerdict(String interviewId);
}