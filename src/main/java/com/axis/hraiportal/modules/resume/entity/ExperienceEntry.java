package com.axis.hraiportal.modules.resume.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEntry {

    private String company;
    private String role;
    private String duration;      // e.g. "2021 - 2023"
    private String description;
}
