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

    private String hrId;            // hr_id (uuid, PK)
    private String name;
    private String email;
    private String password;        // stored hashed — never plain text
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}