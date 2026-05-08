package com.axis.hraiportal.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "pinecone")
@Validated
@Data
public class PineconeProperties {

    @NotBlank
    private String apiKey;

    @NotBlank
    private String indexName;

    private String namespace = "resumes";

    @Positive
    private int dimension = 384;
}