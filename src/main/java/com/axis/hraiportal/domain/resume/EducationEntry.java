package com.axis.hraiportal.domain.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {

    private String institution;
    private String degree;
    private String field;
    private String year;
}