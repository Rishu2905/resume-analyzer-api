package com.axis.hraiportal.modules.mockinterview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequestDTO {
    @NotBlank
    private String content;
}
