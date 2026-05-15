package com.axis.hraiportal.common.config;

import com.axis.hraiportal.common.properties.SupabaseProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(SupabaseProperties.class)
public class SupabaseConfig {

    @Bean("supabasePublicClient")
    public WebClient supabasePublicClient(
            SupabaseProperties props,
            ExchangeStrategies exchangeStrategies) {
        return buildWebClient(props, props.getAnonKey(),
                exchangeStrategies);
    }

    @Bean("supabaseAdminClient")
    public WebClient supabaseAdminClient(
            SupabaseProperties props,
            ExchangeStrategies exchangeStrategies) {
        return buildWebClient(props, props.getServiceRoleKey(),
                exchangeStrategies);
    }

    private WebClient buildWebClient(
            SupabaseProperties props,
            String key,
            ExchangeStrategies exchangeStrategies) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) props.getConnectTimeout().toMillis())
                .responseTimeout(props.getReadTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(
                                props.getReadTimeout().toSeconds(),
                                TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(props.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)  // ← key line
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("apikey", key)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + key)
                .defaultHeader("Prefer", "return=representation")
                .build();
    }
}
