package com.stoliar.microservice_exchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.services.LimitService;
import com.stoliar.microservice_exchange.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BankControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @Mock
    private LimitService limitService;

    @InjectMocks
    private BankController bankController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bankController).build();

        // Настраиваем ObjectMapper для работы с ZonedDateTime
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void addTransaction_ShouldReturnTransaction_WhenValidRequest() throws Exception {
        // Создаем тестовую транзакцию
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountFrom(1001L);
        transaction.setAccountTo(1002L);
        transaction.setCurrencyShortname("EUR");
        transaction.setSum(BigDecimal.valueOf(500));
        transaction.setSumUsd(BigDecimal.valueOf(500));
        transaction.setExpenseCategory("product");
        transaction.setDatetime(ZonedDateTime.now());
        transaction.setLimitExceeded(false);

        // Мокаем сервис
        when(transactionService.saveTransaction(any(Transaction.class))).thenReturn(transaction);

        // Отправляем запрос и проверяем ответ
        mockMvc.perform(post("/api/bank/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountFrom").value(1001))
                .andExpect(jsonPath("$.accountTo").value(1002))
                .andExpect(jsonPath("$.currencyShortname").value("EUR"))
                .andExpect(jsonPath("$.sum").value(500))
                .andExpect(jsonPath("$.expenseCategory").value("product"))
                .andExpect(jsonPath("$.limitExceeded").value(false));
    }

    @Test
    void setLimit_ShouldReturnLimit_WhenValidRequest() throws Exception {
        // Создаем тестовый лимит
        Limit limit = new Limit();
        limit.setId(1L);
        limit.setAccountFrom(1001L);
        limit.setLimitSum(BigDecimal.valueOf(2000));
        limit.setExpenseCategory("product");
        limit.setLimitDatetime(ZonedDateTime.now());
        limit.setLimitCurrencyShortname("USD");

        // Мокаем сервис
        when(limitService.setLimit(any(Limit.class))).thenReturn(limit);

        // Отправляем запрос и проверяем ответ
        mockMvc.perform(post("/api/bank/limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(limit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountFrom").value(1001))
                .andExpect(jsonPath("$.limitSum").value(2000))
                .andExpect(jsonPath("$.expenseCategory").value("product"))
                .andExpect(jsonPath("$.limitCurrencyShortname").value("USD"));
    }
}