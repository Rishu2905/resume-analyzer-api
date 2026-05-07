package com.axis.hraiportal.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettings mongoClientSettings(
            @Value("${spring.data.mongodb.uri}") String uri) {

        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToConnectionPoolSettings(pool -> pool
                        .maxSize(15)
                        .minSize(3)
                        .maxWaitTime(2000, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(60, TimeUnit.SECONDS))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000, TimeUnit.MILLISECONDS))
                .applyToServerSettings(server -> server
                        .heartbeatFrequency(10, TimeUnit.SECONDS))
                .build();
    }
}