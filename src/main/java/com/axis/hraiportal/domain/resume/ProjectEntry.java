package com.axis.hraiportal.domain.resume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntry {
    private String name;
    private List<String> bullets;
    private List<String> technologies;
}