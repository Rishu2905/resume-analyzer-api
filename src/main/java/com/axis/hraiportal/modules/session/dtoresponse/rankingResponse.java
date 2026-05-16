package com.axis.hraiportal.modules.session.dtoresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class rankingResponse {
    private String sessionId;
    private String jobTitle;
    private int totalResumes;
    private List<RankedResume> rankings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankedResume {
        private String documentId;
        private String filename;
        private String mongoId;
        private Integer score;
       // private String recommendation;
    }
}