package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import lombok.Data;

/**
 * Class to represent instruments: stocks, ETF etc
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Instrument {

    private String trackingId;
    private String status;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private String figi;
        private String ticker;
        private String isin;
        private double minPriceIncrement;
        private int lot;
        private Currency currency;
        private String name;
        private InstrumentType type;
    }
}
