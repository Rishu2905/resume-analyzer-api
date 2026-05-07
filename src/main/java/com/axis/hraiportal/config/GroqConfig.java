package com.axis.hraiportal.config;

import com.axis.hraiportal.properties.GroqProperties;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
@EnableConfigurationProperties(GroqProperties.class)
public class GroqConfig {

    // Groq runs Llama 3.3 70B — OpenAI-compatible /chat/completions API
    // Used for: JD parsing, resume scoring, interview question generation
    @Bean("groqWebClient")
    public WebClient groqWebClient(GroqProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) props.getConnectTimeout().toMillis())
                .responseTimeout(props.getReadTimeout());

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + props.getApiKey())
                .build();
    }
}