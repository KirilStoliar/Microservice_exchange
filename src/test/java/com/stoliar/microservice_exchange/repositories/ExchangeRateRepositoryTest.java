package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class ExchangeRateRepositoryTest {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private final String CURRENCY_PAIR = "EUR/USD";
    private final LocalDate DATE1 = LocalDate.of(2024, 2, 12);
    private final LocalDate DATE2 = LocalDate.of(2024, 2, 13);

    @BeforeEach
    void setUp() {
        exchangeRateRepository.save(new ExchangeRate(CURRENCY_PAIR, new BigDecimal("1.05"), DATE1, new BigDecimal("1.05"), new BigDecimal("1.04")));
        exchangeRateRepository.save(new ExchangeRate(CURRENCY_PAIR, new BigDecimal("1.055"), DATE2, new BigDecimal("1.055"), new BigDecimal("1.053")));
    }

    @Test
    void shouldFindExchangeRateByCurrencyPairAndDate() {
        Optional<ExchangeRate> result = exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, DATE1);

        assertTrue(result.isPresent());
        assertEquals(CURRENCY_PAIR, result.get().getCurrencyPair());
        assertEquals(DATE1, result.get().getDate());
        assertEquals(new BigDecimal("1.05"), result.get().getClose());
    }

    @Test
    void shouldReturnEmptyIfExchangeRateNotFound() {
        Optional<ExchangeRate> result = exchangeRateRepository.findByCurrencyPairAndDate("KZT/USD", DATE1);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindLatestExchangeRateByCurrencyPair() {
        Optional<ExchangeRate> result = exchangeRateRepository.findFirstByCurrencyPairOrderByDateDesc(CURRENCY_PAIR);

        assertTrue(result.isPresent());
        assertEquals(DATE2, result.get().getDate()); // Должен быть самый последний курс (2024-02-13)
        assertEquals(new BigDecimal("1.055"), result.get().getClose());
    }

    @Test
    void shouldReturnEmptyIfNoDataAvailableForCurrencyPair() {
        Optional<ExchangeRate> result = exchangeRateRepository.findFirstByCurrencyPairOrderByDateDesc("KZT/USD");
        assertTrue(result.isEmpty());
    }
}