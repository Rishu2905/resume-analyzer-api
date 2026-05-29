package com.axis.hraiportal.modules.hr.dtoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @JsonProperty("hr_id")
    private String hrId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("company")
    private String company;

    @JsonProperty("token")
    private String token;

    @JsonProperty("token_type")
    private String tokenType;
}