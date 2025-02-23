package com.stoliar.microservice_exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
@Schema(description = "Информация о транзакции, превысившей лимит")
public class ExceededTransactionResponseDTO {

    @Schema(description = "Идентификатор счёта отправителя", example = "1234567890")
    private Long accountFrom;

    @Schema(description = "Идентификатор счёта получателя", example = "9876543210")
    private Long accountTo;

    @Schema(description = "Код валюты транзакции", example = "EUR")
    private String currencyShortname;

    @Schema(description = "Сумма транзакции", example = "10000.45")
    private BigDecimal sum;

    @Schema(description = "Категория расходов", example = "product")
    private String expenseCategory;

    @Schema(description = "Дата и время транзакции", example = "2022-01-30T00:00:00+06:00")
    private ZonedDateTime datetime;

    @Schema(description = "Сумма лимита", example = "1000.00")
    private BigDecimal limitSum;

    @Schema(description = "Дата и время установления лимита", example = "2022-01-10T00:00:00+06:00")
    private ZonedDateTime limitDatetime;

    @Schema(description = "Код валюты лимита", example = "USD")
    private String limitCurrencyShortname;

    public ExceededTransactionResponseDTO(Long accountFrom, Long accountTo, String currencyShortname, BigDecimal sum,
                                          String expenseCategory, ZonedDateTime datetime, BigDecimal limitSum,
                                          ZonedDateTime limitDatetime, String limitCurrencyShortname) {
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.currencyShortname = currencyShortname;
        this.sum = sum;
        this.expenseCategory = expenseCategory;
        this.datetime = datetime;
        this.limitSum = limitSum;
        this.limitDatetime = limitDatetime;
        this.limitCurrencyShortname = limitCurrencyShortname;
    }
}