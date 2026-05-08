package com.axis.hraiportal.domain.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "resumes")  // ← MongoDB collection name
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDocument {

    @Id
    private String id;                    // MongoDB ObjectId

    @Field("candidate_id")
    private String candidateId;           // links to Supabase hr table

    @Field("file_name")
    private String fileName;

    @Field("raw_text")
    private String rawText;               // full extracted resume text

    @Field("skills")
    private List<String> skills;

    @Field("experience")
    private List<ExperienceEntry> experience;

    @Field("education")
    private List<EducationEntry> education;

    @Field("pinecone_vector_id")
    private String pineconeVectorId;      // links to Pinecone embedding

    @Field("uploaded_at")
    @CreatedDate
    private LocalDateTime uploadedAt;
}