package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LimitRepository limitRepository;


    @Test
    @DisplayName("Поиск транзакций с превышением лимита для accountFrom")
    @Transactional
    @Rollback
    void findExceededTransactionsWithLimits_ShouldReturnExceededTransactions() {

        Long accountFrom = 10L;
        String expenseCategory = "product";

        Limit limit = new Limit();
        limit.setAccountFrom(accountFrom);
        limit.setExpenseCategory(expenseCategory);
        limit.setLimitSum(BigDecimal.valueOf(1500));
        limit.setLimitDatetime(ZonedDateTime.now());

        Transaction normalTransaction = new Transaction();
        normalTransaction.setAccountFrom(accountFrom);
        normalTransaction.setAccountTo(20L);
        normalTransaction.setCurrencyShortname("USD");
        normalTransaction.setSum(BigDecimal.valueOf(1500));
        normalTransaction.setSumUsd(BigDecimal.valueOf(1500));
        normalTransaction.setExpenseCategory(expenseCategory);
        normalTransaction.setDatetime(ZonedDateTime.now().minusHours(3));
        normalTransaction.setLimitExceeded(false);

        Transaction exceededTransaction = new Transaction();
        exceededTransaction.setAccountFrom(accountFrom);
        exceededTransaction.setAccountTo(20L);
        exceededTransaction.setCurrencyShortname("USD");
        exceededTransaction.setSum(BigDecimal.valueOf(1500));
        exceededTransaction.setSumUsd(BigDecimal.valueOf(1600));
        exceededTransaction.setExpenseCategory(expenseCategory);
        exceededTransaction.setDatetime(ZonedDateTime.now());
        exceededTransaction.setLimitExceeded(true);

        transactionRepository.save(normalTransaction);
        transactionRepository.save(exceededTransaction);
        limitRepository.save(limit);
    }
}