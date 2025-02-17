package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.DTO.TwelveDataResponse;
import com.stoliar.microservice_exchange.configs.TwelveDataConfig;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.repositories.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TwelveDataService {

    private static final Logger logger = LoggerFactory.getLogger(TwelveDataService.class);

    @Autowired
    private TwelveDataService(RestTemplate restTemplate, TwelveDataConfig twelveDataConfig,
                              ExchangeRateRepository repository) {
        this.restTemplate = restTemplate;
        this.twelveDataConfig = twelveDataConfig;
        this.exchangeRateRepository = repository;
    }

    private final TwelveDataConfig twelveDataConfig;

    private final ExchangeRateRepository exchangeRateRepository;

    private final RestTemplate restTemplate;

    public ExchangeRate getExchangeRateFromApi(String currencyPair) {
        if (currencyPair == null) {
            logger.error("Currency pair is null");
            throw new IllegalArgumentException("Currency pair cannot be null");
        }

        // Получаем текущую дату
        LocalDate today = LocalDate.now();

        // Проверяем, есть ли данные для текущей даты в базе данных
        Optional<ExchangeRate> existingRateOpt = exchangeRateRepository
                .findByCurrencyPairAndDate(currencyPair, today);

        if (existingRateOpt.isPresent()) {
            // Если данные уже есть в базе, возвращаем их
            logger.debug("Data for currency pair {} and date {} already exists in the database", currencyPair, today);
            return existingRateOpt.get();
        }

        // Если данных нет, выполняем запрос к API
        // Формируем URL для запроса
        String url = UriComponentsBuilder.fromHttpUrl(twelveDataConfig.getApiUrl() + "/quote")
                .queryParam("symbol", currencyPair)
                .queryParam("interval", "1day")
                .queryParam("apikey", twelveDataConfig.getApiKey())
                .toUriString();

        logger.debug("Fetching exchange rate from URL: {}", url);

        try {
            // Выполняем запрос к API
            TwelveDataResponse response = restTemplate.getForObject(url, TwelveDataResponse.class);

            if (response == null) {
                logger.error("Received null response from Twelve Data API");
                throw new RuntimeException("Received null response from Twelve Data API");
            }

            // Проверяем, что данные корректны
            if (response.getClose() == null || response.getDatetime() == null || response.getPreviousClose() == null) {
                logger.error("Invalid response data: close, datetime, or previous_close is null");
                throw new RuntimeException("Invalid response data: close, datetime, or previous_close is null");
            }

            // Создаем объект ExchangeRate
            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyPair,
                    response.getClose(),
                    response.getDatetime(),
                    response.getClose(),
                    response.getPreviousClose()
            );

            // Сохраняем в базу данных
            exchangeRateRepository.save(exchangeRate);
            logger.debug("Saved exchange rate to database: {}", exchangeRate);

            return exchangeRate;
        } catch (Exception e) {
            logger.error("Failed to fetch exchange rate from Twelve Data API", e);
            throw new RuntimeException("Failed to fetch exchange rate from Twelve Data API", e);
        }
    }
}