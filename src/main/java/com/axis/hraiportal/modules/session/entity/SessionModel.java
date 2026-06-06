package com.axis.hraiportal.modules.session.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionModel {

    private String sessionId;       // session_id (uuid, PK)
    private String userId;
    @NotBlank(message = "Title cannot be empty")// hr_id (FK → hrs)
    private String title;           // session title e.g. "Backend hiring Q2"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @JsonProperty("is_deleted")
    private boolean deleted;
}
