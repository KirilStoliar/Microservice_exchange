package com.stoliar.microservice_exchange.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "exchange_rates",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"currency_pair", "date"})}
)
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_pair", length = 7, nullable = false)
    private String currencyPair;

    @Column(name = "rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "close", nullable = false, precision = 18, scale = 6)
    private BigDecimal close;

    @Column(name = "previous_close", nullable = false, precision = 18, scale = 6)
    private BigDecimal previousClose;

    public ExchangeRate(String currencyPair, BigDecimal rate, LocalDate date, BigDecimal close, BigDecimal previousClose) {
        this.currencyPair = currencyPair;
        this.rate = rate;
        this.date = date;
        this.close = close;
        this.previousClose = previousClose;
    }
}