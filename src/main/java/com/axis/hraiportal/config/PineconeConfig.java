package com.axis.hraiportal.config;

import com.axis.hraiportal.properties.PineconeProperties;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PineconeProperties.class)
@Slf4j
public class PineconeConfig {

    // Pinecone stores resume embeddings (float[768])
    // Each vector: { id: resumeId, values: float[], metadata: {candidateId} }
    @Bean
    public Pinecone pineconeClient(PineconeProperties props) {
        log.info("Initializing Pinecone client → index: {}",
                props.getIndexName());
        return new Pinecone.Builder(props.getApiKey()).build();
    }

    @Bean
    public Index pineconeIndex(Pinecone pinecone,
                               PineconeProperties props) {
        return pinecone.getIndexConnection(props.getIndexName());
    }
}