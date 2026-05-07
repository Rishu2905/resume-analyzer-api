package com.axis.hraiportal.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "huggingface")
@Validated
@Data
public class HuggingFaceProperties {

    @NotBlank
    private String apiKey;

    private String baseUrl = "https://api-inference.huggingface.co";

    private String embeddingModel =
            "sentence-transformers/all-mpnet-base-v2";

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration connectTimeout = Duration.ofSeconds(5);

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(30);
}