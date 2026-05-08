package com.axis.hraiportal.domain.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDocument {

    @Id
    private String id;                // MongoDB ObjectId — this is the mongo_id
    // stored in Supabase documents table

    @Field("filename")
    private String filename;          // mirrors documents.filename

    @Field("raw_text")
    private String rawText;           // full extracted resume text

    @Field("skills")
    private List<String> skills;

    @Field("experience")
    private List<ExperienceEntry> experience;

    @Field("education")
    private List<EducationEntry> education;

    @Field("pinecone_vector_id")
    private String pineconeVectorId;

    @Field("uploaded_at")
    private LocalDateTime uploadedAt;
}