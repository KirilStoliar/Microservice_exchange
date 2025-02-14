package com.stoliar.microservice_exchange.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TwelveDataResponse {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("name")
    private String name;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("mic_code")
    private String micCode;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("datetime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate datetime;

    @JsonProperty("close")
    private BigDecimal close;

    @JsonProperty("previous_close")
    private BigDecimal previousClose;

    @JsonProperty("change")
    private BigDecimal change;

    @JsonProperty("percent_change")
    private BigDecimal percentChange;

    @JsonProperty("volume")
    private Long volume;

    @JsonProperty("average_volume")
    private Long averageVolume;

    @JsonProperty("is_market_open")
    private Boolean isMarketOpen;

    @JsonProperty("fifty_two_week")
    private FiftyTwoWeek fiftyTwoWeek;

    @Getter
    @Setter
    public static class FiftyTwoWeek {
        @JsonProperty("low")
        private BigDecimal low;

        @JsonProperty("high")
        private BigDecimal high;

        @JsonProperty("low_change")
        private BigDecimal lowChange;

        @JsonProperty("high_change")
        private BigDecimal highChange;

        @JsonProperty("low_change_percent")
        private BigDecimal lowChangePercent;

        @JsonProperty("high_change_percent")
        private BigDecimal highChangePercent;

        @JsonProperty("range")
        private String range;
    }
}
