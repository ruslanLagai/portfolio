package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.project.portfolio.model.Currency;

/**
 * Class to represent
 *  expectedYield
 *  averagePositionPrice
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AveragePositionItem (Currency currency, double value) {
}
