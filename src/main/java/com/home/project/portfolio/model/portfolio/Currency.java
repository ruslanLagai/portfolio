package com.home.project.portfolio.model.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Class to represent position item in portfolio
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Currency {
    private com.home.project.portfolio.model.Currency currency;
    private double balance;
    private double blocked;
}
