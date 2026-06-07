package com.axis.hraiportal.modules.document.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRecord {

    private String documentId;      // document_id (uuid, PK)
    private String sessionId;       // session_id (FK → sessions)
    private String userId;            // user_id (FK → hrs)
    private String filename;        // original file name
    private String fileHash;        // hash to detect duplicates
    private String mongoId;         // links to ResumeDocument._id in MongoDB
    private LocalDateTime uploadedAt;
    private String jobTitle;
    private Integer score;
    private String recommendation;
    @JsonProperty("is_deleted")
    private boolean deleted;

}
