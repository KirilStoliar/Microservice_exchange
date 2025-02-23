package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.dto.TwelveDataResponse;
import com.stoliar.microservice_exchange.configs.TwelveDataConfig;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.repositories.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwelveDataServiceTest {

    @Mock
    private TwelveDataConfig twelveDataConfig;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Spy
    private RestTemplate restTemplate;

    @InjectMocks
    private TwelveDataService twelveDataService;

    private final String CURRENCY_PAIR = "eur/USD";
    private final LocalDate TODAY = LocalDate.now();
    private final String API_URL = "https://api.twelvedata.com";
    private final String API_KEY = "f39d556aadf44616bb095a338069c5e6";

    @BeforeEach
    void setUp() {
        lenient().when(twelveDataConfig.getApiUrl()).thenReturn(API_URL);
        lenient().when(twelveDataConfig.getApiKey()).thenReturn(API_KEY);
    }

    @Test
    void testGetExchangeRateFromApi_WhenDataExistsInDatabase() {
        ExchangeRate existingRate = new ExchangeRate(CURRENCY_PAIR, BigDecimal.valueOf(1.12), TODAY, BigDecimal.valueOf(1.12), BigDecimal.valueOf(1.11));

        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, TODAY)).thenReturn(Optional.of(existingRate));

        ExchangeRate result = twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR);

        assertNotNull(result);
        assertEquals(existingRate, result);
        verify(exchangeRateRepository, times(1)).findByCurrencyPairAndDate(CURRENCY_PAIR, TODAY);
        verifyNoInteractions(restTemplate); // Запрос к API не должен выполняться
    }

    @Test
    void testGetExchangeRateFromApi_WhenDataNotInDatabase() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, TODAY)).thenReturn(Optional.empty());

        ExchangeRate expectedRate = new ExchangeRate(CURRENCY_PAIR, null, TODAY, null, null);

        ExchangeRate result = twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR);

        assertNotNull(result);
        assertEquals(expectedRate.getCurrencyPair(), result.getCurrencyPair());
        assertNotNull(result.getRate());
        assertNotNull(result.getPreviousClose());
        assertEquals(expectedRate.getDate(), result.getDate());

        verify(exchangeRateRepository, times(1)).save(any(ExchangeRate.class));
    }

    @Test
    void testGetExchangeRateFromApi_WhenCurrencyPairIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            twelveDataService.getExchangeRateFromApi(null);
        });

        assertEquals("Currency pair cannot be null", exception.getMessage());
    }

    @Test
    void testGetExchangeRateFromApi_WhenApiResponseIsNull() {
        when(exchangeRateRepository.findByCurrencyPairAndDate(anyString(), any(LocalDate.class))).thenReturn(Optional.empty());

        doReturn(null).when(restTemplate).getForObject(anyString(), eq(TwelveDataResponse.class));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR);
        });
        assertEquals("Failed to fetch exchange rate from Twelve Data API", exception.getMessage());
    }

    @Test
    void testGetExchangeRateFromApi_WhenApiResponseIsInvalid() {
        TwelveDataResponse invalidResponse = new TwelveDataResponse();
        invalidResponse.setDatetime(null);
        invalidResponse.setClose(null);
        invalidResponse.setPreviousClose(null);

        when(exchangeRateRepository.findByCurrencyPairAndDate(CURRENCY_PAIR, TODAY)).thenReturn(Optional.empty());

        String url = API_URL + "/quote?symbol=" + CURRENCY_PAIR + "&interval=1day&apikey=" + API_KEY;
        when(restTemplate.getForObject(url, TwelveDataResponse.class)).thenReturn(invalidResponse);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            twelveDataService.getExchangeRateFromApi(CURRENCY_PAIR);
        });

        assertEquals("Failed to fetch exchange rate from Twelve Data API", exception.getMessage());
    }
}