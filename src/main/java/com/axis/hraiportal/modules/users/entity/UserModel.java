package com.axis.hraiportal.modules.users.entity;

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
public class UserModel {
    @JsonProperty("user_id")
    private String userId; // hr_id (uuid, PK)

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;        // stored hashed — never plain text

    @JsonProperty("company")
    private String company;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("role")
    private String role;
    @JsonProperty("is_deleted")

    private Boolean isDeleted = false;

}
