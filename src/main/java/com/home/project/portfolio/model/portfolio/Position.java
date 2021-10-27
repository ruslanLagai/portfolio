package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.portfolio.model.InstrumentType;
import lombok.Data;

/**
 * Class to represent position item in portfolio
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {
    private String figi;
    private String ticker;
    private String isin;
    private InstrumentType instrumentType;
    private double balance;
    private double blocked;
    private int lots;
    private String name;
    private AveragePositionItem averagePositionPrice;
    private AveragePositionItem averagePositionPriceNoNkd;
    private AveragePositionItem expectedYield;
}
