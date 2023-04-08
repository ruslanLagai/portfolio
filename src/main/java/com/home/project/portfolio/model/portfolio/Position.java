package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.portfolio.model.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class to represent position item in portfolio
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private AveragePositionItem expectedYield;
}
