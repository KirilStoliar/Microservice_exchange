package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = """
        SELECT t.account_from, t.account_to, t.currency_shortname, t.sum, t.expense_category, t.datetime,
               l.limit_sum, l.limit_datetime, l.limit_currency_shortname
        FROM transactions t
        JOIN limits l ON t.account_from = l.account_from AND t.expense_category = l.expense_category
        WHERE t.limit_exceeded = true
        AND t.account_from = :accountFrom
        ORDER BY t.datetime
        """, nativeQuery = true)
    List<Object[]> findExceededTransactionsWithLimits(@Param("accountFrom") Long accountFrom);

}
