package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.dto.ExceededTransactionProjection;
import com.stoliar.microservice_exchange.dto.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.repositories.LimitRepository;
import com.stoliar.microservice_exchange.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LimitRepository limitRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Limit limit;
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setAccountFrom(1L);
        transaction.setAccountTo(2L);
        transaction.setCurrencyShortname("eur");
        transaction.setSum(BigDecimal.valueOf(100));
        transaction.setExpenseCategory("product");
        transaction.setDatetime(ZonedDateTime.now());

        limit = new Limit();
        limit.setAccountFrom(1L);
        limit.setExpenseCategory("product");
        limit.setLimitSum(BigDecimal.valueOf(1000));
        limit.setLimitDatetime(ZonedDateTime.now());
        limit.setLimitCurrencyShortname("USD");

        exchangeRate = new ExchangeRate("eur/USD", BigDecimal.ONE, ZonedDateTime.now().toLocalDate(), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void saveTransaction_ShouldSaveTransactionWithLimitNotExceeded() {
        when(exchangeRateService.getExchangeRate(anyString(), any(LocalDate.class))).thenReturn(exchangeRate);
        when(limitRepository.findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(anyLong(), anyString()))
                .thenReturn(Optional.of(limit));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertNotNull(savedTransaction);
        assertFalse(savedTransaction.isLimitExceeded());
        verify(transactionRepository, times(1)).save(transaction);
        verify(limitRepository, times(1)).save(limit);
    }

    @Test
    void saveTransaction_ShouldSaveTransactionWithLimitExceeded() {
        doReturn(transaction).when(transactionRepository).save(any(Transaction.class));
        doReturn(Optional.of(limit))
                .when(limitRepository)
                .findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(anyLong(), anyString());
        doReturn(exchangeRate)
                .when(exchangeRateService)
                .getExchangeRate(anyString(), any(LocalDate.class));
        transaction.setSum(BigDecimal.valueOf(1500));

        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertNotNull(savedTransaction);
        assertTrue(savedTransaction.isLimitExceeded());
        verify(transactionRepository, times(1)).save(transaction);
        verify(limitRepository, times(1)).save(limit);
    }

    @Test
    void saveTransaction_ShouldCreateNewLimitIfNotExists() {
        when(exchangeRateService.getExchangeRate(anyString(), any(LocalDate.class))).thenReturn(exchangeRate);
        doReturn(Optional.of(limit))
                .when(limitRepository)
                .findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(anyLong(), anyString());
        doReturn(transaction).when(transactionRepository).save(any(Transaction.class));
        when(limitRepository.save(any(Limit.class))).thenReturn(limit);

        Transaction savedTransaction = transactionService.saveTransaction(transaction);

        assertNotNull(savedTransaction);
        verify(limitRepository, times(1)).save(any(Limit.class));
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(Collections.singletonList(transaction));

        List<Transaction> transactions = transactionService.getAllTransactions();

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void getTransactionById_ShouldReturnTransaction() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        Transaction foundTransaction = transactionService.getTransactionById(1L);

        assertNotNull(foundTransaction);
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_ShouldReturnNullIfNotFound() {
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        Transaction foundTransaction = transactionService.getTransactionById(1L);

        assertNull(foundTransaction);
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getExceededTransactions_ShouldReturnExceededTransactions() {
        ExceededTransactionProjection projection = mock(ExceededTransactionProjection.class);

        when(projection.getAccountFrom()).thenReturn(1L);
        when(projection.getAccountTo()).thenReturn(2L);
        when(projection.getCurrencyShortname()).thenReturn("USD");
        when(projection.getSum()).thenReturn(BigDecimal.valueOf(100));
        when(projection.getExpenseCategory()).thenReturn("Groceries");
        when(projection.getDatetime()).thenReturn(Instant.now());
        when(projection.getLimitSum()).thenReturn(BigDecimal.valueOf(1000));
        when(projection.getLimitDatetime()).thenReturn(Instant.now());
        when(projection.getLimitCurrencyShortname()).thenReturn("USD");

        List<ExceededTransactionProjection> projections = new ArrayList<>();
        projections.add(projection);

        when(transactionRepository.findExceededTransactionsWithLimits(anyLong())).thenReturn(projections);

        List<ExceededTransactionResponseDTO> exceededTransactions = transactionService.getExceededTransactions(1L);

        assertNotNull(exceededTransactions);
        assertEquals(1, exceededTransactions.size());

        ExceededTransactionResponseDTO dto = exceededTransactions.get(0);
        assertEquals(1L, dto.getAccountFrom());
        assertEquals(2L, dto.getAccountTo());
        assertEquals("USD", dto.getCurrencyShortname());
        assertEquals(BigDecimal.valueOf(100), dto.getSum());
        assertEquals("Groceries", dto.getExpenseCategory());
        assertEquals(BigDecimal.valueOf(1000), dto.getLimitSum());
        assertEquals("USD", dto.getLimitCurrencyShortname());

        verify(transactionRepository, times(1)).findExceededTransactionsWithLimits(1L);
    }
}