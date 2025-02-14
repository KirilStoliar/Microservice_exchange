package com.stoliar.microservice_exchange.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwelveDataConfig {

    @Value("${twelvedata.api-key}")
    private String apiKey;

    @Value("${twelvedata.base-url}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}