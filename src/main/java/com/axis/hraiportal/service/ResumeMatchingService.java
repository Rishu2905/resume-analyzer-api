package com.axis.hraiportal.service;

import com.axis.hraiportal.client.GroqClient;
import com.axis.hraiportal.client.HuggingFaceClient;
import com.axis.hraiportal.client.PineconeClient;
import com.axis.hraiportal.domain.jd.JobDescriptionModel;
import com.axis.hraiportal.domain.resume.ResumeDocument;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeMatchingService {

    private final HuggingFaceClient huggingFaceClient;
    private final PineconeClient pineconeClient;
    private final GroqClient groqClient;
    private final DocumentService documentService;
    private final JobDescriptionService jdService;

    // ── Core RAG Pipeline ────────────────────────────────────
    // 1. fetch JD from Supabase
    // 2. embed JD text via HuggingFace
    // 3. query Pinecone for top-K similar resumes
    // 4. fetch those resumes from MongoDB
    // 5. send to Groq for scoring + reasoning
    public Mono<String> matchResumesToJd(
            String jdId, int topK) {

        return jdService.getById(jdId)
                .flatMap(jd -> {
                    log.info("Matching resumes for JD: {}",
                            jd.getTitle());

                    // Step 2 — embed the JD description
                    return huggingFaceClient
                            .embed(jd.getDescription())
                            .flatMap(jdEmbedding -> {

                                // Step 3 — query Pinecone
                                QueryResponseWithUnsignedIndices
                                        queryResponse = pineconeClient
                                        .querySimilar(jdEmbedding, topK);

                                List<String> mongoIds = queryResponse
                                        .getMatchesList()
                                        .stream()
                                        .map(ScoredVectorWithUnsignedIndices::getId)
                                        .toList();

                                log.info("Found {} similar resumes",
                                        mongoIds.size());

                                // Step 4 — fetch resumes from MongoDB
                                List<ResumeDocument> resumes = mongoIds
                                        .stream()
                                        .map(documentService::getResumeByMongoId)
                                        .toList();

                                // Step 5 — send to Groq for scoring
                                return groqClient.complete(
                                        buildSystemPrompt(jd),
                                        buildUserPrompt(jd, resumes));
                            });
                });
    }

    // ── Prompts ──────────────────────────────────────────────
    private String buildSystemPrompt(JobDescriptionModel jd) {
        return """
            You are an expert HR recruiter.
            Your job is to evaluate resumes against a job description
            and provide a match score (0-100) with reasoning.
            Be concise, objective and structured in your response.
            """;
    }

    private String buildUserPrompt(
            JobDescriptionModel jd,
            List<ResumeDocument> resumes) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Job Title: ").append(jd.getTitle())
                .append("\n\n");
        prompt.append("Job Description:\n")
                .append(jd.getDescription())
                .append("\n\n");
        prompt.append("Resumes to evaluate:\n\n");

        for (int i = 0; i < resumes.size(); i++) {
            ResumeDocument r = resumes.get(i);
            prompt.append("Resume ").append(i + 1).append(":\n");
            prompt.append("Skills: ")
                    .append(String.join(", ", r.getSkills()))
                    .append("\n");
            prompt.append("Raw Text: ")
                    .append(r.getRawText(), 0,
                            Math.min(500, r.getRawText().length()))
                    .append("...\n\n");
        }

        prompt.append("""
            For each resume provide:
            1. Match score (0-100)
            2. Top 3 matching skills
            3. Key gaps
            4. One line recommendation
            """);

        return prompt.toString();
    }
}