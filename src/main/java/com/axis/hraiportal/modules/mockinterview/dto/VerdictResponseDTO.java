package com.axis.hraiportal.modules.mockinterview.dto;

import lombok.Data;

import java.util.List;

@Data
public class VerdictResponseDTO {
    private String verdictId;
    private String interviewId;
    private Integer score;
    private String verdict;
    private String overallPerformance;
    private List<String> strongAreas;
    private List<String> weakAreas;
    private String recommendation;
    private String createdAt;
}