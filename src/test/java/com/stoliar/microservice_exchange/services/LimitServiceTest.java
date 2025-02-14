package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.repositories.LimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimitServiceTest {

    @Mock
    private LimitRepository limitRepository;

    @InjectMocks
    private LimitService limitService;

    private Limit limit;

    @BeforeEach
    void setup() {
        limit = new Limit();
        limit.setAccountFrom(1L);
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(ZonedDateTime.now());
        limit.setLimitCurrencyShortname("USD");
        limit.setExpenseCategory("product");
    }

    @Test
    void setLimit_ShouldSaveAndReturnLimit() {
        // Настройка мока
        when(limitRepository.save(any(Limit.class))).thenReturn(limit);

        // Вызываем метод
        Limit savedLimit = limitService.setLimit(limit);

        // Проверяем, что возвращаемый объект не null
        assertNotNull(savedLimit);
        assertEquals(limit, savedLimit);

        // Проверяем, что метод save() вызывался один раз
        verify(limitRepository, times(1)).save(limit);
    }
}