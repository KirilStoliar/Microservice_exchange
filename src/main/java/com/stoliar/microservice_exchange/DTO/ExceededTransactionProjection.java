package com.stoliar.microservice_exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

public interface ExceededTransactionProjection {

    Long getAccountFrom();

    Long getAccountTo();

    String getCurrencyShortname();

    BigDecimal getSum();

    String getExpenseCategory();

    Instant getDatetime();

    BigDecimal getLimitSum();

    Instant getLimitDatetime();

    String getLimitCurrencyShortname();
}