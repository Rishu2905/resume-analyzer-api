package com.axis.hraiportal.domain.jd;

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

    private String id;
    private String title;
    private String company;
    private String requirements;      // full JD text — fed into HuggingFace for embedding
    private String postedBy;          // HR user id
    private LocalDateTime createdAt;
}