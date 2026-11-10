package com.alonso.explorersaga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    private static final String USER_AGENT = "ExplorerSaga/1.0 (+https://github.com/AsKrueger/ExplorerSaga-Extremadura)";

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${explorersaga.providers.overpass.timeout-ms:30000}") long timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder
                // Overpass pide identificar las aplicaciones cliente para no despriorizar sus peticiones
                .defaultHeader("User-Agent", USER_AGENT)
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
