package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.repositories.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private TwelveDataService twelveDataService;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private final String CURRENCY_PAIR = "eur/USD";
    private final LocalDate DATE = LocalDate.now();
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        exchangeRate = new ExchangeRate(CURRENCY_PAIR, new BigDecimal("1.05"), DATE, new BigDecimal("1.05"), new BigDecimal("1.03"));
    }

    @Test
    void shouldReturnExchangeRateFromDatabase() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, DATE))
                .thenReturn(Optional.of(exchangeRate));

        ExchangeRate result = exchangeRateService.getExchangeRate(CURRENCY_PAIR, DATE);

        assertNotNull(result);
        assertEquals(CURRENCY_PAIR, result.getCurrencyPair());
        assertEquals(new BigDecimal("1.05"), result.getClose());
        verify(exchangeRateRepository, times(1)).findByCurrencyPairAndDate(CURRENCY_PAIR, DATE);
        verifyNoInteractions(twelveDataService);
    }

    @Test
    void shouldFetchExchangeRateFromApiWhenNotInDatabase() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, DATE)).thenReturn(Optional.empty());
        when(twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR)).thenReturn(exchangeRate);

        ExchangeRate result = exchangeRateService.getExchangeRate(CURRENCY_PAIR, DATE);

        assertNotNull(result);
        assertEquals(CURRENCY_PAIR, result.getCurrencyPair());
        verify(twelveDataService, times(1)).getExchangeRateFromApi(CURRENCY_PAIR);
        verify(exchangeRateRepository, times(1)).save(exchangeRate);
    }

    @Test
    void shouldUseLastAvailableExchangeRateIfApiFails() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, DATE)).thenReturn(Optional.empty());
        when(twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR)).thenThrow(new RuntimeException("API error"));
        when(exchangeRateRepository.findFirstByCurrencyPairOrderByDateDesc(CURRENCY_PAIR)).thenReturn(Optional.of(exchangeRate));

        ExchangeRate result = exchangeRateService.getExchangeRate(CURRENCY_PAIR, DATE);

        assertNotNull(result);
        assertEquals(CURRENCY_PAIR, result.getCurrencyPair());
        assertEquals(DATE, result.getDate());
        assertEquals(new BigDecimal("1.05"), result.getClose());
        verify(exchangeRateRepository, times(1)).findFirstByCurrencyPairOrderByDateDesc(CURRENCY_PAIR);
    }

    @Test
    void shouldReturnDefaultExchangeRateIfNoDataAvailable() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, DATE)).thenReturn(Optional.empty());
        when(twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR)).thenThrow(new RuntimeException("API error"));
        when(exchangeRateRepository.findFirstByCurrencyPairOrderByDateDesc(CURRENCY_PAIR)).thenReturn(Optional.empty());

        ExchangeRate result = exchangeRateService.getExchangeRate(CURRENCY_PAIR, DATE);

        assertNotNull(result);
        assertEquals(CURRENCY_PAIR, result.getCurrencyPair());
        assertEquals(DATE, result.getDate());
        assertEquals(BigDecimal.ONE, result.getClose());
        assertEquals(BigDecimal.ONE, result.getPreviousClose());
    }

    @Test
    void shouldThrowExceptionWhenCurrencyPairOrDateIsNull() {
        Exception exception1 = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.getExchangeRate(null, DATE));
        assertEquals("Currency pair and date cannot be null", exception1.getMessage());

        Exception exception2 = assertThrows(IllegalArgumentException.class, () ->
                exchangeRateService.getExchangeRate(CURRENCY_PAIR, null));
        assertEquals("Currency pair and date cannot be null", exception2.getMessage());
    }
}