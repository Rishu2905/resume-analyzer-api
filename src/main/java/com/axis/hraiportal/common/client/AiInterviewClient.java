package com.axis.hraiportal.common.client;

import com.axis.hraiportal.modules.mockinterview.dto.InterviewResponseDTO;
import com.axis.hraiportal.modules.mockinterview.dto.VerdictResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@Slf4j
public class AiInterviewClient {

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    private final WebClient webClient;

    public AiInterviewClient(@Qualifier("aiServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<InterviewResponseDTO> startInterview(
            String userId,
            String docId, String documentId) {
        return webClient.post()
                .uri(aiServiceUrl + "/api/candidate/interview/start")
                .bodyValue(Map.of(
                        "user_id", userId,
                        "doc_id", docId
                ))
                .retrieve()
                .bodyToMono(InterviewResponseDTO.class)
                .doOnError(e ->
                        log.error("Failed to start interview for userId: {}", userId, e)
                );
    }

    public Mono<InterviewResponseDTO> sendMessage(
            String interviewId,
            String content) {
        return webClient.post()
                .uri(aiServiceUrl + "/api/candidate/interview/" + interviewId + "/message")
                .bodyValue(Map.of("content", content))
                .retrieve()
                .bodyToMono(InterviewResponseDTO.class)
                .doOnError(e ->
                        log.error("Failed to send message for interviewId: {}", interviewId, e)
                );
    }

    public Mono<VerdictResponseDTO> getVerdict(String interviewId) {
        return webClient.get()
                .uri(aiServiceUrl + "/ai/interview/" + interviewId + "/verdict")
                .retrieve()
                .bodyToMono(VerdictResponseDTO.class)
                .doOnError(e ->
                        log.error("Failed to get verdict for interviewId: {}", interviewId, e)
                );
    }
}