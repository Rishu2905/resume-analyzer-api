package com.axis.hraiportal.modules.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDocumentResponse {
    private String documentId;
    private String userId;
    private String filename;
    private String mongoId;
    private String jobTitle;
    private String recommendation;
    private String message;
    private LocalDateTime uploadedAt;

}
