package com.axis.hraiportal.modules.document.entity;

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
    private String hrId;            // hr_id (FK → hrs)
    private String filename;        // original file name
    private String fileHash;        // hash to detect duplicates
    private String mongoId;         // links to ResumeDocument._id in MongoDB
    private LocalDateTime uploadedAt;
    private String jobTitle;
    private Integer score;
    private String recommendation;

}
