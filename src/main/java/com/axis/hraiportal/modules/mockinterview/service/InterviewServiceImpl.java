package com.axis.hraiportal.modules.mockinterview.service;

import com.axis.hraiportal.common.client.AiInterviewClient;
import com.axis.hraiportal.modules.document.repository.DocumentRepository;
import com.axis.hraiportal.modules.mockinterview.dto.InterviewResponseDTO;
import com.axis.hraiportal.modules.mockinterview.dto.MessageRequestDTO;
import com.axis.hraiportal.modules.mockinterview.dto.VerdictResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final AiInterviewClient aiInterviewClient;
    private final DocumentRepository documentRepository;

    @Override
    public Mono<InterviewResponseDTO> startInterview(String userId) {
        return documentRepository.findLatestByUserId(userId)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("No resume found. Please upload resume first.")
                ))
                .flatMap(doc -> {
                    log.info(
                            "Starting interview — userId: {}, mongoId: {},documentId:{}",
                            userId, doc.getMongoId(),doc.getDocumentId()
                    );
                    return aiInterviewClient.startInterview(
                            userId,
                            doc.getMongoId(),
                            doc.getDocumentId()
                    );
                });
    }

    @Override
    public Mono<InterviewResponseDTO> sendMessage(
            String interviewId,
            String content) {
        log.info("Sending message for interviewId: {}", interviewId);
        return aiInterviewClient.sendMessage(interviewId, content);
    }

    @Override
    public Mono<VerdictResponseDTO> getVerdict(String interviewId) {
        log.info("Getting verdict for interviewId: {}", interviewId);
        return aiInterviewClient.getVerdict(interviewId);
    }
}