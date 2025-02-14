package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LimitRepository extends JpaRepository<Limit, Long> {

    // Возвращает последний лимит для указанного accountFrom и expenseCategory
    Optional<Limit> findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(Long accountFrom, String expenseCategory);
}
