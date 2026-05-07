package com.axis.hraiportal.config;

import com.axis.hraiportal.properties.HuggingFaceProperties;
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
@EnableConfigurationProperties(HuggingFaceProperties.class)
public class HuggingFaceConfig {

    // HuggingFace generates embeddings from resume + JD text
    // Embeddings feed into Pinecone for semantic similarity search
    @Bean("huggingFaceWebClient")
    public WebClient huggingFaceWebClient(HuggingFaceProperties props) {
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