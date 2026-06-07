package com.axis.hraiportal.modules.jobdescription.entity;

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
public class JobDescriptionModel {

    private String jdId;            // jd_id (uuid, PK)
    private String sessionId;       // session_id (FK → sessions)
    private String userId;            // user_id (FK → hrs)
    private String title;           // job title e.g. "Senior Java Developer"
    private String description;     // full JD text — fed into HuggingFace
    private LocalDateTime createdAt;
    @JsonProperty("is_deleted")
    private boolean deleted;
}
