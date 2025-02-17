package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.Limit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
class LimitRepositoryTest {

    @Autowired
    private LimitRepository limitRepository;

    @Test
    @DisplayName("Поиск последнего лимита для accountFrom и expenseCategory")
    void findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc_ShouldReturnLatestLimit() {

        Long accountFrom = 10L;
        String expenseCategory = "product";

        Limit previousLimit = new Limit();
        previousLimit.setAccountFrom(accountFrom);
        previousLimit.setExpenseCategory(expenseCategory);
        previousLimit.setLimitSum(BigDecimal.valueOf(1500));
        previousLimit.setLimitDatetime(ZonedDateTime.now().minusDays(3));

        Limit newLimit = new Limit();
        newLimit.setAccountFrom(accountFrom);
        newLimit.setExpenseCategory(expenseCategory);
        newLimit.setLimitSum(BigDecimal.valueOf(2000));
        newLimit.setLimitDatetime(ZonedDateTime.now());

        limitRepository.save(previousLimit);
        limitRepository.save(newLimit);

        Optional<Limit> limit = limitRepository.findFirstByAccountFromAndExpenseCategoryOrderByLimitDatetimeDesc(accountFrom, expenseCategory);

        assertTrue(limit.isPresent());
        assertEquals(limit.get().getLimitSum(), BigDecimal.valueOf(2000));
        assertEquals(limit.get().getLimitDatetime(), newLimit.getLimitDatetime());
    }
}