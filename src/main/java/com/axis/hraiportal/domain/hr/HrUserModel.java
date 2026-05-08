package com.axis.hraiportal.domain.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrUserModel {

    private String id;
    private String name;
    private String email;
    private String role;             // "ADMIN" or "RECRUITER"
    private LocalDateTime createdAt;
}