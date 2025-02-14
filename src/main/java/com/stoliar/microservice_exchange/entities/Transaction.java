package com.stoliar.microservice_exchange.entities;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @Column(name = "account_from", nullable = false)
    private Long accountFrom;

    @Column(name = "account_to", nullable = false)
    private Long accountTo;

    @Column(name = "currency_shortname", length = 3, nullable = false)
    private String currencyShortname;

    @Column(name = "sum", nullable = false, precision = 18, scale = 2)
    private BigDecimal sum;

    @Column(name = "sum_usd", nullable = false, precision = 18, scale = 2)
    @Hidden
    private BigDecimal sumUsd;

    @Column(name = "expense_category", length = 10, nullable = false)
    private String expenseCategory;  // "product" / "service"

    @Column(name = "datetime", nullable = false)
    private ZonedDateTime datetime;

    @Hidden
    @Column(name = "limit_exceeded", nullable = false)
    private boolean limitExceeded;
}
