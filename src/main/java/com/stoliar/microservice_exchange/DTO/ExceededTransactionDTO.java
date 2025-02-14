package com.stoliar.microservice_exchange.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Setter
public class ExceededTransactionDTO {

    private ZonedDateTime limitDatetime;
    private BigDecimal limitSum;
    private BigDecimal remainingLimit;
    private ZonedDateTime transactionDatetime;
    private BigDecimal transactionSum;
    private boolean limitExceeded;

    public ExceededTransactionDTO(ZonedDateTime limitDatetime, BigDecimal limitSum, BigDecimal remainingLimit,
                                  ZonedDateTime transactionDatetime, BigDecimal transactionSum, boolean limitExceeded) {
        this.limitDatetime = limitDatetime;
        this.limitSum = limitSum;
        this.remainingLimit = remainingLimit;
        this.transactionDatetime = transactionDatetime;
        this.transactionSum = transactionSum;
        this.limitExceeded = limitExceeded;
    }
}
