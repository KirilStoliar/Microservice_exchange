package com.stoliar.microservice_exchange.entities;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@ToString
@Entity
@Table(name = "limits")
@Getter
@Setter
public class Limit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @Column(name = "account_from", nullable = false)
    private Long accountFrom;

    @Column(name = "limit_sum", nullable = false, precision = 18, scale = 2)
    private BigDecimal limitSum = BigDecimal.valueOf(1000); // Значение по умолчанию

    @Column(name = "expense_category", length = 10, nullable = false)
    private String expenseCategory;

    @Column(name = "limit_datetime", nullable = false, updatable = false)
    @Hidden
    private ZonedDateTime limitDatetime = ZonedDateTime.now();

    @Column(name = "limit_currency_shortname", length = 3, nullable = false)
    @Hidden
    private String limitCurrencyShortname = "USD";
}
