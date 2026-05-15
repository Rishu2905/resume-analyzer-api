package com.axis.hraiportal.common.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "groq")
@Validated
@Data
public class GroqProperties {

    @NotBlank
    private String apiKey;

    private String baseUrl = "https://api.groq.com/openai/v1";

    private String model = "llama-3.3-70b-versatile";

    @Positive
    private int maxTokens = 4096;

    private double temperature = 0.7;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration connectTimeout = Duration.ofSeconds(5);

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(60);
}
