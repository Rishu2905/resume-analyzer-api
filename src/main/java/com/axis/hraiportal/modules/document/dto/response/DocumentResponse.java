package com.axis.hraiportal.modules.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private String documentId;
    private String sessionId;
    private String userId;
    private String filename;
    private String mongoId;
    private Integer score;
    private List<String> matchingSkills;
    private List<String> gaps;
    private String recommendation;
    private String message;
}
