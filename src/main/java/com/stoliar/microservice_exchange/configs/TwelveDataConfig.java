package com.stoliar.microservice_exchange.configs;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TwelveDataConfig {

    @Value("${twelvedata.api-key}")
    private String apiKey;

    @Value("${twelvedata.base-url}")
    private String apiUrl;
}