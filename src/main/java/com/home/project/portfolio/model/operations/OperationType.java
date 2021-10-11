package com.home.project.portfolio.model.operations;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class to represent operation type in Tinkoff
 */
@AllArgsConstructor
@Getter
public enum OperationType {
    BUY("Buy"),
    SELL("Sell"),
    COMMISSION("BrokerCommission"),
    DIVIDEND_TAX("TaxDividend"),
    DIVIDEND("Dividend");

    private final String period;

 }
