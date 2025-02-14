package com.stoliar.microservice_exchange.repositories;

import com.stoliar.microservice_exchange.entities.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Находит курс валюты по паре и дате.
     *
     * @param currencyPair Пара валют (например, "KZT/USD").
     * @param date         Дата курса.
     * @return Курс валюты, если найден.
     */
    Optional<ExchangeRate> findByCurrencyPairAndDate(String currencyPair, LocalDate date);

    /**
     * Находит последний доступный курс валюты по паре.
     *
     * @param currencyPair Пара валют (например, "KZT/USD").
     * @return Последний доступный курс валюты.
     */
    Optional<ExchangeRate> findFirstByCurrencyPairOrderByDateDesc(String currencyPair);
}