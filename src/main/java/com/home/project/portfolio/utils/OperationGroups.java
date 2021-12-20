package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.operations.OperationType;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

import static com.home.project.portfolio.model.operations.OperationType.*;

/**
 * Utility class to group operations
 */
public class OperationGroups {

    public static final EnumSet<OperationType> TRADING_OPERATIONS = EnumSet.of(BUY, SELL, DIVIDEND,
            COUPON, TAX_DIVIDEND, BUY_CARD);
    public static final EnumSet<OperationType> NON_TRADING_OPERATIONS = EnumSet.of(REPAYMENT, PART_REPAYMENT,
            TAX, TAX_BACK, TAX_LUCRE, MARGIN_COMMISSION, EXCHANGE_COMMISSION, SERVICE_COMMISSION,
            OTHER_COMMISSION, PAY_IN, PAY_OUT, SECURITY_IN, SECURITY_OUT);
    public static final EnumSet<OperationType> COMMISSIONS = EnumSet.of(BROKER_COMMISSION, MARGIN_COMMISSION,
            SERVICE_COMMISSION, EXCHANGE_COMMISSION, OTHER_COMMISSION);
    public static final EnumSet<OperationType> TAXES = EnumSet.of(TAX, TAX_BACK, TAX_LUCRE, TAX_COUPON, TAX_DIVIDEND);
    public static final EnumSet<OperationType> PAYMENTS = EnumSet.of(PAY_IN, PAY_OUT);

    public static final Set<String> PAYMENT_TICKERS = Set.of(Constants.PAY_IN_RUB, Constants.PAY_IN_USD,
            Constants.PAY_IN_EUR, Constants.PAY_OUT_USD, Constants.PAY_OUT_RUB, Constants.PAY_OUT_EUR);
}
