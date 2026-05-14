package com.axis.hraiportal.modules.resume.entity;

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
    private String id;

    @Field("filename")
    private String filename;

    @Field("contact")
    private ContactInfo contact;

    @Field("summary")
    private String summary;

    @Field("skills")
    private List<String> skills;

    @Field("experience")
    private List<ExperienceEntry> experience;

    @Field("projects")
    private List<ProjectEntry> projects;

    @Field("education")
    private List<EducationEntry> education;

    // scoring
    @Field("score")
    private Integer score;

    @Field("matching_skills")
    private List<String> matchingSkills;

    @Field("gaps")
    private List<String> gaps;

    @Field("recommendation")
    private String recommendation;

    @Field("uploaded_at")
    private LocalDateTime uploadedAt;
}
