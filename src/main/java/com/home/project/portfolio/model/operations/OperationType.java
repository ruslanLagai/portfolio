package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.stream.Stream;

/**
 * Class to represent operation type in Tinkoff
 */
@AllArgsConstructor
@Getter
public enum OperationType {
    BUY("Buy"),
    SELL("Sell"),
    COUPON("Coupon"),
    DIVIDEND("Dividend"),
    REPAYMENT("Repayment"),
    PART_REPAYMENT("PartRepayment"),
    TAX("Tax"),
    TAX_BACK("TaxBack"),
    TAX_LUCRE("TaxLucre"),
    TAX_COUPON("TaxCoupon"),
    TAX_DIVIDEND("TaxDividend"),
    BROKER_COMMISSION("BrokerCommission"),
    MARGIN_COMMISSION("MarginCommission"),
    EXCHANGE_COMMISSION("ExchangeCommission"),
    SERVICE_COMMISSION("ServiceCommission"),
    OTHER_COMMISSION("OtherCommission"),
    PAY_IN("PayIn"),
    PAY_OUT("PayOut"),
    BUY_CARD("BuyCard"),
    SECURITY_IN("SecurityIn"),
    SECURITY_OUT("SecurityOut");

    private final String type;

    @JsonCreator
    public static OperationType parse(String value) {
        return Stream.of(values())
                .filter(period -> value.equalsIgnoreCase(period.getType()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
