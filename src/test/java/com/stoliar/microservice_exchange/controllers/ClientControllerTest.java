package com.stoliar.microservice_exchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stoliar.microservice_exchange.DTO.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ClientController clientController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();

        // Настраиваем ObjectMapper для работы с ZonedDateTime
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void getAllTransactions_ShouldReturnTransactionList_WhenTransactionsExist() throws Exception {
        // Создаём список транзакций
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAccountFrom(1001L);
        transaction1.setAccountTo(1002L);
        transaction1.setCurrencyShortname("USD");
        transaction1.setSum(BigDecimal.valueOf(500));
        transaction1.setExpenseCategory("product");
        transaction1.setDatetime(ZonedDateTime.now());

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAccountFrom(2001L);
        transaction2.setAccountTo(2002L);
        transaction2.setCurrencyShortname("EUR");
        transaction2.setSum(BigDecimal.valueOf(300));
        transaction2.setExpenseCategory("service");
        transaction2.setDatetime(ZonedDateTime.now());

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/client/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenTransactionExists() throws Exception {
        // Создаём тестовую транзакцию
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccountFrom(1001L);
        transaction.setAccountTo(1002L);
        transaction.setCurrencyShortname("USD");
        transaction.setSum(BigDecimal.valueOf(500));
        transaction.setExpenseCategory("product");
        transaction.setDatetime(ZonedDateTime.now());

        when(transactionService.getTransactionById(1L)).thenReturn(transaction);

        mockMvc.perform(get("/api/client/transaction/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountFrom").value(1001))
                .andExpect(jsonPath("$.sum").value(500));
    }

    @Test
    void getTransactionById_ShouldReturn404_WhenTransactionDoesNotExist() throws Exception {
        when(transactionService.getTransactionById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/client/transaction/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExceededTransactions_ShouldReturnExceededTransactionList_WhenDataExists() throws Exception {
        // Создаём тестовые данные
        ExceededTransactionResponseDTO exceededTransaction1 = new ExceededTransactionResponseDTO(
                1001L, 1002L, "USD", BigDecimal.valueOf(1200), "product",
                ZonedDateTime.now(), BigDecimal.valueOf(1000), ZonedDateTime.now(), "USD"
        );

        ExceededTransactionResponseDTO exceededTransaction2 = new ExceededTransactionResponseDTO(
                2001L, 2002L, "EUR", BigDecimal.valueOf(1500), "service",
                ZonedDateTime.now(), BigDecimal.valueOf(800), ZonedDateTime.now(), "EUR"
        );

        List<ExceededTransactionResponseDTO> exceededTransactions = Arrays.asList(exceededTransaction1, exceededTransaction2);

        when(transactionService.getExceededTransactions(1001L)).thenReturn(exceededTransactions);

        mockMvc.perform(get("/api/client/exceeded-transactions")
                        .param("accountFrom", "1001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].accountFrom").value(1001))
                .andExpect(jsonPath("$[1].accountFrom").value(2001));
    }
}