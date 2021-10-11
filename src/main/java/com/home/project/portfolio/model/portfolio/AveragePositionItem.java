package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.portfolio.model.Currency;
import lombok.Data;

/**
 * Class to represent
 *  expectedYield
 *  averagePositionPrice
 *  averagePositionPriceNoNkd
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AveragePositionItem {
    private Currency currency;
    private int value;
}
