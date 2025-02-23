package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.repositories.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class ExchangeRateService {

    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, TwelveDataService twelveDataService) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.twelveDataService = twelveDataService;
    }

    private ExchangeRateRepository exchangeRateRepository;

    private TwelveDataService twelveDataService;

    public ExchangeRate getExchangeRate(String currencyPair, LocalDate date) {
        if (currencyPair == null || date == null) {
            log.error("Currency pair or date is null");
            throw new IllegalArgumentException("Currency pair and date cannot be null");
        }

        Optional<ExchangeRate> exchangeRateOpt = exchangeRateRepository.findByCurrencyPairAndDate(currencyPair, date);

        if (exchangeRateOpt.isPresent()) {
            log.debug("Retrieved exchange rate from database: {}", exchangeRateOpt.get());
            return exchangeRateOpt.get();
        } else {
            // Если курс в базе данных отсутствует, запрашиваем его из Twelve Data API
            log.debug("Exchange rate not found in database. Fetching from API...");
            try {
                ExchangeRate exchangeRate = twelveDataService.getExchangeRateFromApi(currencyPair);
                if (exchangeRate == null) {
                    log.error("Failed to fetch exchange rate from API for currency pair: {}", currencyPair);
                    throw new RuntimeException("Failed to fetch exchange rate from API for currency pair: " + currencyPair);
                }
                exchangeRateRepository.save(exchangeRate); // Сохраняем в базу данных
                log.debug("Saved exchange rate to database: {}", exchangeRate);
                return exchangeRate;
            } catch (Exception e) {
                log.error("Failed to fetch exchange rate from API. Using last available rate.", e);
                // Используем последний доступный курс из базы данных
                Optional<ExchangeRate> lastExchangeRateOpt = exchangeRateRepository
                        .findFirstByCurrencyPairOrderByDateDesc(currencyPair);
                if (lastExchangeRateOpt.isPresent()) {
                    ExchangeRate lastExchangeRate = lastExchangeRateOpt.get();
                    log.debug("Using last available exchange rate: {}", lastExchangeRate);
                    return new ExchangeRate(currencyPair, lastExchangeRate.getClose(), date, lastExchangeRate.getClose(), lastExchangeRate.getPreviousClose());
                } else {
                    // Если данных нет вообще, возвращаем курс по умолчанию
                    log.debug("No exchange rate data available. Using default value.");
                    return new ExchangeRate(currencyPair, BigDecimal.ONE, date, BigDecimal.ONE, BigDecimal.ONE);
                }
            }
        }
    }
}