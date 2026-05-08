package com.axis.hraiportal.domain.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionModel {

    private String sessionId;       // session_id (uuid, PK)
    private String hrId;            // hr_id (FK → hrs)
    private String title;           // session title e.g. "Backend hiring Q2"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}