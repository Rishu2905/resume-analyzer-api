package com.axis.hraiportal.modules.document.service;

import com.axis.hraiportal.common.client.GroqClient;
import com.axis.hraiportal.common.util.PdfExtractorUtil;
import com.axis.hraiportal.modules.document.dto.response.CandidateDocumentResponse;
import com.axis.hraiportal.modules.document.dto.response.DocumentResponse;
import com.axis.hraiportal.modules.document.entity.DocumentRecord;
import com.axis.hraiportal.modules.document.repository.DocumentRepository;
import com.axis.hraiportal.modules.jobdescription.repository.JobDescriptionRepository;
import com.axis.hraiportal.modules.resume.entity.*;
import com.axis.hraiportal.modules.resume.repository.ResumeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService
{

    private final ResumeRepository resumeRepository;
    private final DocumentRepository documentRepository;
    private final JobDescriptionRepository jdRepository;
    private final GroqClient groqClient;
    private final PdfExtractorUtil pdfExtractorUtil;
    private final ObjectMapper objectMapper;

    // ── Main upload pipeline ─────────────────────────────────
    public Mono<DocumentResponse> uploadResume(

            String userId,

            String sessionId,

            FilePart file,

            String jobTitle) {

        return DataBufferUtils

                .join(file.content())

                .flatMap(dataBuffer -> {

                            byte[] pdfBytes =
                                    new byte[dataBuffer.readableByteCount()];

                            dataBuffer.read(pdfBytes);

                            DataBufferUtils.release(dataBuffer);

                            String rawText =
                                    pdfExtractorUtil.extractText(pdfBytes);

                            String fileHash =
                                    pdfExtractorUtil.generateHash(pdfBytes);

                            String filename =
                                    file.filename();

//                            log.debug(
//                                    "Processing resume: {}",
//                                    filename);

        // Step 2 — validate ownership + fetch JD
        return jdRepository


                .findAuthorizedJd(
                        userId,
                        sessionId)


                .flatMap(jd -> {
//                    log.debug("reached step 2 in document service");

                    String jdTitle =
                            jd.getTitle();

                    String jobDescription =
                            jd.getDescription();

//                    log.debug(
//                            "Comparing against JD: {}",
//                            jdTitle);

                    // Step 3 — one Groq call:
                    // parse + score
                    return groqClient.complete(

                                    buildSystemPrompt(),

                                    buildUserPrompt(
                                            rawText,
                                            jdTitle,
                                            jobDescription))

                            .flatMap(groqResponse -> {

                                // Step 4 — parse Groq JSON response
                                ResumeDocument resumeDoc =

                                        parseGroqResponse(
                                                groqResponse,
                                                filename);

                                // Step 5 — save to MongoDB
                                ResumeDocument saved =
                                        resumeRepository
                                                .save(resumeDoc);

                                String mongoId =
                                        saved.getId();

//                                log.debug(
//                                        "Resume saved to MongoDB: {}",
//                                        mongoId);

                                // Step 6 — save metadata to Supabase
                                DocumentRecord record =

                                        DocumentRecord.builder()

                                                .documentId(
                                                        UUID.randomUUID()
                                                                .toString())

                                                .sessionId(
                                                        sessionId)

                                                .userId(
                                                        userId)

                                                .filename(
                                                        filename)

                                                .fileHash(
                                                        fileHash)

                                                .mongoId(
                                                        mongoId)

                                                .jobTitle(
                                                        jdTitle)

                                                .score(
                                                        saved.getScore())

                                                .recommendation(
                                                        saved.getRecommendation())

                                                .uploadedAt(
                                                        LocalDateTime.now())

                                                .build();

                                return documentRepository
                                        .save(record)

                                        .map(savedRecord ->

                                                DocumentResponse
                                                        .builder()

                                                        .documentId(
                                                                savedRecord
                                                                        .getDocumentId())

                                                        .sessionId(
                                                                sessionId)

                                                        .userId(
                                                                userId)

                                                        .filename(
                                                                filename)

                                                        .mongoId(
                                                                mongoId)

                                                        .score(
                                                                saved.getScore())

                                                        .matchingSkills(
                                                                saved.getMatchingSkills())

                                                        .gaps(
                                                                saved.getGaps())

                                                        .recommendation(
                                                                saved.getRecommendation())

                                                        .message(
                                                                "Resume processed and scored successfully")

                                                        .build());
                            });
                });
                });
    }



    // ── System prompt ────────────────────────────────────────
    private String buildSystemPrompt() {
        return """
            You are an expert resume parser and HR evaluator.
            Given a resume and a job description, you will:
            1. Parse the resume into structured data
            2. Score the resume against the job description
            Return ONLY valid JSON. No explanation, no markdown,
            no code blocks. Just the raw JSON object.
            """;
    }

    // ── User prompt ──────────────────────────────────────────
    private String buildUserPrompt(
            String rawText,
            String jobTitle,
            String jobDescription) {

        return """
            JOB TITLE: %s
            
            JOB DESCRIPTION:
            %s
            
            RESUME:
            %s
            
            Parse the resume and score it against the JD.
            Return this exact JSON structure:
            {
                "contact": {
                    "name": "",
                    "email": "",
                    "phone": "",
                    "location": ""
                },
                "summary": "",
                "skills": [],
                "experience": [
                    {
                        "company": "",
                        "role": "",
                        "duration": "",
                        "description": ""
                    }
                ],
                "projects": [
                    {
                        "name": "",
                        "bullets": [],
                        "technologies": []
                    }
                ],
                "education": [
                    {
                        "institution": "",
                        "degree": "",
                        "year": ""
                    }
                ],
                "score": 0,
                "matching_skills": [],
                "gaps": [],
                "recommendation": ""
            }
            
            Scoring rules:
            - score is 0-100
            - matching_skills: skills mentioned in projects or overall that match JD. Similar skills should be included too
            - gaps: important skills in JD which are not present anywhere in resume
            - recommendation: whats the candidates ideal job type,is he fit for the job
            uploaded in JD?
            """.formatted(jobTitle, jobDescription, rawText);
    }

    // ── Parse Groq JSON response into ResumeDocument ─────────
    private ResumeDocument parseGroqResponse(
            String response, String filename) {
        try {
            String clean = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode json = objectMapper.readTree(clean);

            // parse contact
            ContactInfo contact = null;
            if (json.has("contact")) {
                JsonNode c = json.get("contact");
                contact = ContactInfo.builder()
                        .name(getText(c, "name"))
                        .email(getText(c, "email"))
                        .phone(getText(c, "phone"))
                        .location(getText(c, "location"))
                        .build();
            }

            // parse skills
            List<String> skills = new ArrayList<>();
            if (json.has("skills")) {
                json.get("skills").forEach(
                        s -> skills.add(s.asText()));
            }

            // parse experience
            List<ExperienceEntry> experience = new ArrayList<>();
            if (json.has("experience")) {
                json.get("experience").forEach(exp ->
                        experience.add(ExperienceEntry.builder()
                                .company(getText(exp, "company"))
                                .role(getText(exp, "role"))
                                .duration(getText(exp, "duration"))
                                .description(getText(exp, "description"))
                                .build()));
            }

            // parse projects
            List<ProjectEntry> projects = new ArrayList<>();
            if (json.has("projects")) {
                json.get("projects").forEach(proj -> {
                    List<String> bullets = new ArrayList<>();
                    List<String> technologies = new ArrayList<>();

                    if (proj.has("bullets"))
                        proj.get("bullets").forEach(
                                b -> bullets.add(b.asText()));

                    if (proj.has("technologies"))
                        proj.get("technologies").forEach(
                                t -> technologies.add(t.asText()));

                    projects.add(ProjectEntry.builder()
                            .name(getText(proj, "name"))
                            .bullets(bullets)
                            .technologies(technologies)
                            .build());
                });
            }

            // parse education
            List<EducationEntry> education = new ArrayList<>();
            if (json.has("education")) {
                json.get("education").forEach(edu ->
                        education.add(EducationEntry.builder()
                                .institution(getText(edu, "institution"))
                                .degree(getText(edu, "degree"))
                                .year(getText(edu, "year"))
                                .build()));
            }

            // parse matching skills
            List<String> matchingSkills = new ArrayList<>();
            if (json.has("matching_skills"))
                json.get("matching_skills").forEach(
                        s -> matchingSkills.add(s.asText()));

            // parse gaps
            List<String> gaps = new ArrayList<>();
            if (json.has("gaps"))
                json.get("gaps").forEach(
                        g -> gaps.add(g.asText()));

            return ResumeDocument.builder()
                    .filename(filename)
                    .contact(contact)
                    .summary(getText(json, "summary"))
                    .skills(skills)
                    .experience(experience)
                    .projects(projects)
                    .education(education)
                    .score(json.has("score")
                            ? json.get("score").asInt() : 0)
                    .matchingSkills(matchingSkills)
                    .gaps(gaps)
                    .recommendation(getText(json, "recommendation"))
                    .uploadedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Resume parsing failed: " + e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        return node.has(field)
                ? node.get(field).asText("") : "";
    }

    // ─────────────────────────────────────────────
// GET DOCUMENTS BY SESSION
// ─────────────────────────────────────────────
    public Mono<List<DocumentRecord>> getBySession(

            String userId,

            String sessionId) {

        // validate ownership first
        return jdRepository

                .findAuthorizedJd(
                        userId,
                        sessionId)

                .flatMap(jd ->
                        documentRepository
                                .findBySessionId(
                                        sessionId,userId));
    }

    // ── Get full analysis for session ────────────────────────
// verifies session belongs to user
// fetches all resumes sorted by score DESC
    public Mono<List<ResumeDocument>> getAnalysis(
            String userId, String sessionId) {

        // Step 1 — verify session belongs to this user
        return jdRepository.findAuthorizedJd(userId, sessionId)

                .flatMap(jd ->
                        // Step 2 — fetch all document records from Supabase
                        documentRepository.findBySessionId(sessionId,userId))

                .flatMap(documents -> {
                    if (documents.isEmpty()) {
                        return Mono.just(List.of());
                    }

                    // Step 3 — extract mongoIds
                    List<String> mongoIds = documents.stream()
                            .sorted(Comparator.comparingInt(
                                    d -> -d.getScore()))  // sort by score DESC
                            .map(DocumentRecord::getMongoId)
                            .toList();

                    // Step 4 — fetch full resume data from MongoDB
                    List<ResumeDocument> resumes = mongoIds.stream()
                            .map(mongoId -> resumeRepository
                                    .findById(mongoId)
                                    .orElse(null))
                            .filter(resume -> resume != null)
                            .toList();

                    return Mono.just(resumes);
                });
    }
    private String candidatePrompt(){
        return """
            You are an expert resume parser.
            Given a resume, you will:
            Parse the resume into structured data
            Return ONLY valid JSON. No explanation, no markdown,
            no code blocks. Just the raw JSON object.
            Format:
            {
                "contact": {
                    "name": "",
                    "email": "",
                    "phone": "",
                    "location": ""
                },
                "summary": "",
                "skills": [],
                "experience": [
                    {
                        "company": "",
                        "role": "",
                        "duration": "",
                        "description": ""
                    }
                ],
                "projects": [
                    {
                        "name": "",
                        "bullets": [],
                        "technologies": []
                    }
                ],
                "education": [
                    {
                        "institution": "",
                        "degree": "",
                        "year": ""
                    }
                ]
            } resume text \n
            """;}
    public Mono<CandidateDocumentResponse> candidateResumeUpload(
        String userId,FilePart file,String jobTitle){
        log.debug("controller hit");
        log.debug(jobTitle);
    return DataBufferUtils

            .join(file.content())

            .flatMap(dataBuffer -> {
                log.debug("reached parsing initilization");

                byte[] pdfBytes =
                        new byte[dataBuffer.readableByteCount()];

                dataBuffer.read(pdfBytes);

                DataBufferUtils.release(dataBuffer);

                String rawText =
                        pdfExtractorUtil.extractText(pdfBytes);
                log.debug("parsing done");

                String fileHash =
                        pdfExtractorUtil.generateHash(pdfBytes);
                log.debug("hashing done");

                String filename =
                        file.filename();
                log.debug("sending to llm");
                return groqClient
                        .complete(

                                candidatePrompt(),rawText)




                        .flatMap(groqResponse -> {
                            log.debug(" flatmap point reached");

                            // Step 4 — parse Groq JSON response
                            log.debug("groq parsing begin");
                            ResumeDocument resumeDoc =

                                    parseGroqResponse(
                                            groqResponse,
                                            filename);

                            // Step 5 — save to MongoDB
                            log.debug("saving to mongo");
                            ResumeDocument saved =
                                    resumeRepository
                                            .save(resumeDoc);

                            String mongoId =
                                    saved.getId();

//                                log.debug(
//                                        "Resume saved to MongoDB: {}",
//                                        mongoId);

                            // Step 6 — save metadata to Supabase
                            log.debug("step 6");
                            DocumentRecord record =

                                    DocumentRecord.builder()

                                            .documentId(
                                                    UUID.randomUUID()
                                                            .toString())

                                            .sessionId(
                                                    null)

                                            .userId(
                                                    userId)

                                            .filename(
                                                    filename)

                                            .fileHash(
                                                    fileHash)

                                            .mongoId(
                                                    mongoId)

                                            .jobTitle(
                                                    jobTitle)

                                            .score(
                                                    null)

                                            .recommendation(
                                                    null)

                                            .uploadedAt(
                                                    LocalDateTime.now())

                                            .build();
                            log.debug("reached document repository");
                            return documentRepository

                                    .save(record)

                                    .map(savedRecord ->


                                            CandidateDocumentResponse

                                                    .builder()

                                                    .documentId(
                                                            savedRecord
                                                                    .getDocumentId())

                                                    .userId(
                                                            userId)

                                                    .filename(
                                                            filename)

                                                    .mongoId(
                                                            mongoId)
                                                    .jobTitle(jobTitle)

                                                    .message(
                                                            "Resume processed and scored successfully")
                                                    .uploadedAt(LocalDateTime.now())

                                                    .build());
            });

});
}}
