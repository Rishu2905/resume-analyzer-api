package com.axis.hraiportal.modules.document.service;

import com.axis.hraiportal.common.client.GroqClient;
import com.axis.hraiportal.common.util.PdfExtractorUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final ResumeRepository resumeRepository;
    private final DocumentRepository documentRepository;
    private final JobDescriptionRepository jdRepository;
    private final GroqClient groqClient;
    private final PdfExtractorUtil pdfExtractorUtil;
    private final ObjectMapper objectMapper;

    // ── Main upload pipeline ─────────────────────────────────
    public Mono<DocumentResponse> uploadResume(
            String sessionId, String hrId,
            MultipartFile file, String jobTitle) {

        // Step 1 — extract text from PDF
        String rawText = pdfExtractorUtil.extractText(file);
        String fileHash = pdfExtractorUtil.generateHash(file);
        String filename = file.getOriginalFilename();

        log.debug("Processing resume: {}", filename);                          // debug log

        // Step 2 — fetch JD from session
        return jdRepository.findBySessionId(sessionId)
                .flatMap(jds -> {
                    if (jds.isEmpty()) {
                        return Mono.error(new RuntimeException(
                                "No JD found for session: " + sessionId +
                                        ". Please create a JD before uploading resumes."));
                    }

                    var jd = jds.getFirst();
                    String jdTitle = jd.getTitle(); // chatgpt fix
                    String jobDescription = jd.getDescription();

                    log.debug("Comparing against JD: {}", jdTitle);                // debug log

                    // Step 3 — one Groq call: parse + score
                    return groqClient.complete(
                                    buildSystemPrompt(),
                                    buildUserPrompt(rawText, jdTitle, jobDescription))

                            .flatMap(groqResponse -> {

                                // Step 4 — parse Groq JSON response
                                ResumeDocument resumeDoc =
                                        parseGroqResponse(
                                                groqResponse, filename);

                                // Step 5 — save to MongoDB
                                ResumeDocument saved =
                                        resumeRepository.save(resumeDoc);
                                String mongoId = saved.getId();
                                log.debug("Resume saved to MongoDB: {}",           // debug log
                                        mongoId);

                                // Step 6 — save metadata to Supabase
                                DocumentRecord record =
                                        DocumentRecord.builder()
                                                .documentId(
                                                        UUID.randomUUID().toString())
                                                .sessionId(sessionId)
                                                .hrId(hrId)
                                                .filename(filename)
                                                .fileHash(fileHash)
                                                .mongoId(mongoId)
                                                .jobTitle(jdTitle)
                                                .score(saved.getScore())
                                                .uploadedAt(LocalDateTime.now())
                                                .build();

                                return documentRepository.save(record)
                                        .map(savedRecord ->
                                                DocumentResponse.builder()
                                                        .documentId(
                                                                savedRecord.getDocumentId())
                                                        .sessionId(sessionId)
                                                        .hrId(hrId)
                                                        .filename(filename)
                                                        .mongoId(mongoId)
                                                        .score(saved.getScore())
                                                        .matchingSkills(
                                                                saved.getMatchingSkills())
                                                        .gaps(saved.getGaps())
                                                        .recommendation(
                                                                saved.getRecommendation())
                                                        .message("Resume processed " +
                                                                "and scored successfully")
                                                        .build());
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
            - matching_skills: skills in resume that match JD
            - gaps: important skills in JD missing from resume
            - recommendation: one sentence hiring recommendation
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

    // ── Get documents by session ─────────────────────────────
    public Mono<List<DocumentRecord>> getBySession(
            String sessionId) {
        return documentRepository.findBySessionId(sessionId);
    }

    // ── Get full resume analysis from MongoDB ─────────────────
    public ResumeDocument getAnalysis(String mongoId) {
        return resumeRepository.findById(mongoId)
                .orElseThrow(() -> new RuntimeException(
                        "Resume not found: " + mongoId));
    }
}
