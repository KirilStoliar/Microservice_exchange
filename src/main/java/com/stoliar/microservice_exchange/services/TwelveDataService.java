package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.dto.TwelveDataResponse;
import com.stoliar.microservice_exchange.configs.TwelveDataConfig;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.repositories.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TwelveDataService {

    private final TwelveDataConfig twelveDataConfig;

    private final ExchangeRateRepository exchangeRateRepository;

    private final RestTemplate restTemplate;

    public ExchangeRate getExchangeRateFromApi(String currencyPair) {
        if (currencyPair == null) {
            log.error("Currency pair is null");
            throw new IllegalArgumentException("Currency pair cannot be null");
        }

        LocalDate today = LocalDate.now();

        Optional<ExchangeRate> existingRateOpt = exchangeRateRepository
                .findByCurrencyPairAndDate(currencyPair, today);

        if (existingRateOpt.isPresent()) {
            // Если данные уже есть в базе, возвращаем их
            log.debug("Data for currency pair {} and date {} already exists in the database", currencyPair, today);
            return existingRateOpt.get();
        }

        // Если данных нет, выполняем запрос к API
        String url = UriComponentsBuilder.fromHttpUrl(twelveDataConfig.getApiUrl() + "/quote")
                .queryParam("symbol", currencyPair)
                .queryParam("interval", "1day")
                .queryParam("apikey", twelveDataConfig.getApiKey())
                .toUriString();

        log.debug("Fetching exchange rate from URL: {}", url);

        try {
            TwelveDataResponse response = restTemplate.getForObject(url, TwelveDataResponse.class);

            if (response == null) {
                log.error("Received null response from Twelve Data API");
                throw new RuntimeException("Received null response from Twelve Data API");
            }

            // Проверяем, что данные корректны
            if (response.getClose() == null || response.getDatetime() == null || response.getPreviousClose() == null) {
                log.error("Invalid response data: close, datetime, or previous_close is null");
                throw new RuntimeException("Invalid response data: close, datetime, or previous_close is null");
            }

            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyPair,
                    response.getClose(),
                    response.getDatetime(),
                    response.getClose(),
                    response.getPreviousClose()
            );

            exchangeRateRepository.save(exchangeRate);
            log.debug("Saved exchange rate to database: {}", exchangeRate);

            return exchangeRate;
        } catch (Exception e) {
            log.error("Failed to fetch exchange rate from Twelve Data API", e);
            throw new RuntimeException("Failed to fetch exchange rate from Twelve Data API", e);
        }
    }
}