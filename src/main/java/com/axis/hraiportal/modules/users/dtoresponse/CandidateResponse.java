package com.axis.hraiportal.modules.users.dtoresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor



public class CandidateResponse {

        @JsonProperty("user_id")
        private String userId;
        private String name;
        private String email;
        private List<String> document_id;
}
