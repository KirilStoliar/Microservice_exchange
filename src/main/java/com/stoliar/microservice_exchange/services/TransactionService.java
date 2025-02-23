package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.dto.ExceededTransactionProjection;
import com.stoliar.microservice_exchange.dto.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.repositories.LimitRepository;
import com.stoliar.microservice_exchange.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionService(TransactionRepository transactionRepository, LimitRepository limitRepository, ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
        this.exchangeRateService = exchangeRateService;
    }

    private TransactionRepository transactionRepository;

    private LimitRepository limitRepository;

    private ExchangeRateService exchangeRateService;

    public Transaction saveTransaction(Transaction transaction) {
        if (transaction == null) {
            log.error("Transaction is null");
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        log.info("Saving transaction: {}", transaction);

        // Получаем текущий курс валюты транзакции к USD
        LocalDate today = LocalDate.now();
        String currencyPair = transaction.getCurrencyShortname() + "/USD";
        ExchangeRate exchangeRate;

        try {
            exchangeRate = exchangeRateService.getExchangeRate(currencyPair, today);
        } catch (RuntimeException e) {
            log.error("Failed to fetch exchange rate from API. Using default value.", e);
            // Используем значение по умолчанию, если запрос к API завершился ошибкой
            exchangeRate = new ExchangeRate(currencyPair, BigDecimal.ONE, today, BigDecimal.ONE, BigDecimal.ONE);
        }

        log.debug("Using exchange rate: {}", exchangeRate);

        // Конвертируем сумму транзакции в USD
        BigDecimal sumUsd = transaction.getSum().multiply(exchangeRate.getClose());
        transaction.setSumUsd(sumUsd);
        log.debug("Converted sum to USD: {}", sumUsd);

        // Проверяем лимит для данной категории расходов
        Limit limit = limitRepository.findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(
                transaction.getAccountFrom(),
                transaction.getExpenseCategory()
        ).orElseGet(() -> {
            // Если лимит не установлен, создаем новый с значением по умолчанию
            Limit newLimit = new Limit();
            newLimit.setAccountFrom(transaction.getAccountFrom());
            newLimit.setExpenseCategory(transaction.getExpenseCategory());
            newLimit.setLimitSum(BigDecimal.valueOf(1000)); // Лимит по умолчанию
            newLimit.setLimitDatetime(ZonedDateTime.now());
            log.debug("Created new limit with default value: {}", newLimit);
            return limitRepository.save(newLimit);
        });

        log.debug("Retrieved limit: {}", limit);

        // Проверяем, превышает ли сумма транзакции оставшийся лимит
        BigDecimal remainingLimit = limit.getLimitSum().subtract(transaction.getSumUsd());
        if (remainingLimit.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setLimitExceeded(true);
            log.debug("Limit exceeded for transaction: {}", transaction);
        } else {
            transaction.setLimitExceeded(false);
            log.debug("Limit not exceeded for transaction: {}", transaction);
        }

        // Обновляем лимит
        limit.setLimitSum(remainingLimit);
        limitRepository.save(limit);
        log.debug("Updated limit: {}", limit);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.debug("Saved transaction: {}", savedTransaction);

        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<ExceededTransactionResponseDTO> getExceededTransactions(Long accountFrom) {
        List<ExceededTransactionProjection> results = transactionRepository.findExceededTransactionsWithLimits(accountFrom);

        return results.stream()
                .map(result ->
                        new ExceededTransactionResponseDTO(
                                result.getAccountFrom(),
                                result.getAccountTo(),
                                result.getCurrencyShortname(),
                                result.getSum(),
                                result.getExpenseCategory(),
                                result.getDatetime().atZone(ZoneId.systemDefault()),
                                result.getLimitSum(),
                                result.getLimitDatetime().atZone(ZoneId.systemDefault()),
                                result.getLimitCurrencyShortname()
                        ))
                .collect(Collectors.toList());
    }
}
