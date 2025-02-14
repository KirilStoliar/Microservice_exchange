package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.DTO.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.ExchangeRate;
import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.repositories.LimitRepository;
import com.stoliar.microservice_exchange.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionService (TransactionRepository transactionRepository, LimitRepository limitRepository, ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
        this.exchangeRateService = exchangeRateService;
    }

    private TransactionRepository transactionRepository;

    private LimitRepository limitRepository;

    private ExchangeRateService exchangeRateService;

    public Transaction saveTransaction(Transaction transaction) {
        if (transaction == null) {
            logger.error("Transaction is null");
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Логирование входных данных
        logger.info("Saving transaction: {}", transaction);

        // Получаем текущий курс валюты транзакции к USD
        LocalDate today = LocalDate.now();
        String currencyPair = transaction.getCurrencyShortname() + "/USD";
        ExchangeRate exchangeRate;

        try {
            exchangeRate = exchangeRateService.getExchangeRate(currencyPair, today);
        } catch (RuntimeException e) {
            logger.error("Failed to fetch exchange rate from API. Using default value.", e);
            // Используем значение по умолчанию, если запрос к API завершился ошибкой
            exchangeRate = new ExchangeRate(currencyPair, BigDecimal.ONE, today, BigDecimal.ONE, BigDecimal.ONE);
        }

        logger.debug("Using exchange rate: {}", exchangeRate);

        // Конвертируем сумму транзакции в USD
        BigDecimal sumUsd = transaction.getSum().multiply(exchangeRate.getClose());
        transaction.setSumUsd(sumUsd);
        logger.debug("Converted sum to USD: {}", sumUsd);

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
            logger.debug("Created new limit with default value: {}", newLimit);
            return limitRepository.save(newLimit);
        });

        logger.debug("Retrieved limit: {}", limit);

        // Проверяем, превышает ли сумма транзакции оставшийся лимит
        BigDecimal remainingLimit = limit.getLimitSum().subtract(transaction.getSumUsd());
        if (remainingLimit.compareTo(BigDecimal.ZERO) < 0) {
            transaction.setLimitExceeded(true);
            logger.debug("Limit exceeded for transaction: {}", transaction);
        } else {
            transaction.setLimitExceeded(false);
            logger.debug("Limit not exceeded for transaction: {}", transaction);
        }

        // Обновляем лимит
        limit.setLimitSum(remainingLimit);
        limitRepository.save(limit);
        logger.debug("Updated limit: {}", limit);

        // Сохраняем транзакцию
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.debug("Saved transaction: {}", savedTransaction);

        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    public List<ExceededTransactionResponseDTO> getExceededTransactions(Long accountFrom) {
        List<Object[]> results = transactionRepository.findExceededTransactionsWithLimits(accountFrom);

        return results.stream()
                .map(result -> {
                    // Преобразуем Instant в ZonedDateTime
                    ZonedDateTime transactionDatetime = ((Instant) result[5]).atZone(ZoneId.systemDefault());
                    ZonedDateTime limitDatetime = ((Instant) result[7]).atZone(ZoneId.systemDefault());

                    return new ExceededTransactionResponseDTO(
                            ((Number) result[0]).longValue(), // accountFrom
                            ((Number) result[1]).longValue(), // accountTo
                            (String) result[2], // currencyShortname
                            (BigDecimal) result[3], // sum
                            (String) result[4], // expenseCategory
                            transactionDatetime, // datetime
                            (BigDecimal) result[6], // limitSum
                            limitDatetime, // limitDatetime
                            (String) result[8] // limitCurrencyShortname
                    );
                })
                .collect(Collectors.toList());
    }
}
