package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.dto.ExceededTransactionProjection;
import com.stoliar.microservice_exchange.dto.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = """
            SELECT t.account_from AS accountFrom,
                   t.account_to AS accountTo,
                   t.currency_shortname AS currencyShortname,
                   t.sum AS sum,
                   t.expense_category AS expenseCategory,
                   t.datetime AS datetime,
                   l.limit_sum AS limitSum,
                   l.limit_datetime AS limitDatetime,
                   l.limit_currency_shortname AS limitCurrencyShortname
            FROM transactions t
            JOIN limits l ON t.account_from = l.account_from AND t.expense_category = l.expense_category
            WHERE t.limit_exceeded = true
            AND t.account_from = :accountFrom
            ORDER BY t.datetime
            """, nativeQuery = true)
    List<ExceededTransactionProjection> findExceededTransactionsWithLimits(@Param("accountFrom") Long accountFrom);

}
