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
public class hrResponse {
    @JsonProperty("hr_id")
    private String hrId;
    private String name;
    private String email;
    private String company;
}
