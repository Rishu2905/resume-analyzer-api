package com.axis.hraiportal.modules.mockinterview.dto;

import lombok.Data;

import java.util.List;

@Data
public class InterviewResponseDTO {
    private String interviewId;
    private String type;      // "question" or "assessment"
    private String message;   // question text

    // assessment fields — null when type = "question"
    private Integer score;
    private String verdict;
    private String overallPerformance;
    private String verdictReason;
    private List<String> strongAreas;
    private List<String> weakAreas;
    private String recommendation;
    private List<String> suggestedTopicsToStudy;
}