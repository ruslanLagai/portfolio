package com.home.project.portfolio.utils;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utility class to group operations
 */
public class OperationGroups {

    public static final EnumSet<OperationType> BUY_OPERATIONS = EnumSet.of(
        OperationType.OPERATION_TYPE_BUY, OperationType.OPERATION_TYPE_BUY_CARD,
        OperationType.OPERATION_TYPE_BUY_MARGIN, OperationType.OPERATION_TYPE_DELIVERY_BUY);
    public static final EnumSet<OperationType> SELL_OPERATIONS = EnumSet.of(
        OperationType.OPERATION_TYPE_SELL, OperationType.OPERATION_TYPE_SELL_CARD,
        OperationType.OPERATION_TYPE_SELL_MARGIN, OperationType.OPERATION_TYPE_DELIVERY_SELL);
    public static final EnumSet<OperationType> TRADING_OPERATIONS = EnumSet.of(
        OperationType.OPERATION_TYPE_BUY, OperationType.OPERATION_TYPE_SELL,
        OperationType.OPERATION_TYPE_BUY_CARD, OperationType.OPERATION_TYPE_SELL_CARD,
        OperationType.OPERATION_TYPE_BUY_MARGIN, OperationType.OPERATION_TYPE_SELL_MARGIN,
        OperationType.OPERATION_TYPE_ACCRUING_VARMARGIN, OperationType.OPERATION_TYPE_WRITING_OFF_VARMARGIN,
        OperationType.OPERATION_TYPE_DIVIDEND, OperationType.OPERATION_TYPE_COUPON,
        OperationType.OPERATION_TYPE_DIV_EXT, OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON);
    public static final EnumSet<OperationType> COMMISSIONS = EnumSet.of(
        OperationType.OPERATION_TYPE_BROKER_FEE, OperationType.OPERATION_TYPE_MARGIN_FEE,
        OperationType.OPERATION_TYPE_SERVICE_FEE, OperationType.OPERATION_TYPE_ADVICE_FEE,
        OperationType.OPERATION_TYPE_CASH_FEE, OperationType.OPERATION_TYPE_OUT_FEE,
        OperationType.OPERATION_TYPE_SUCCESS_FEE);
    public static final EnumSet<OperationType> SERVICE_COMMISSIONS = EnumSet.of(
        OperationType.OPERATION_TYPE_MARGIN_FEE, OperationType.OPERATION_TYPE_SERVICE_FEE,
        OperationType.OPERATION_TYPE_ADVICE_FEE, OperationType.OPERATION_TYPE_CASH_FEE, OperationType.OPERATION_TYPE_OUT_FEE,
        OperationType.OPERATION_TYPE_SUCCESS_FEE);
    public static final EnumSet<OperationType> TAXES = EnumSet.of(
        OperationType.OPERATION_TYPE_TAX, OperationType.OPERATION_TYPE_TAX_CORRECTION,
        OperationType.OPERATION_TYPE_TAX_PROGRESSIVE, OperationType.OPERATION_TYPE_TAX_REPO,
        OperationType.OPERATION_TYPE_BENEFIT_TAX, OperationType.OPERATION_TYPE_BOND_TAX,
        OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON, OperationType.OPERATION_TYPE_BOND_TAX_PROGRESSIVE,
        OperationType.OPERATION_TYPE_TAX_REPO_HOLD, OperationType.OPERATION_TYPE_TAX_REPO_HOLD_PROGRESSIVE,
        OperationType.OPERATION_TYPE_TAX_REPO_REFUND, OperationType.OPERATION_TYPE_TAX_REPO_REFUND_PROGRESSIVE,
        OperationType.OPERATION_TYPE_DIVIDEND_TAX, OperationType.OPERATION_TYPE_TAX_CORRECTION_PROGRESSIVE);
    public static final EnumSet<OperationType> PAYMENTS = EnumSet.of(
        OperationType.OPERATION_TYPE_INPUT, OperationType.OPERATION_TYPE_INPUT_SWIFT,
        OperationType.OPERATION_TYPE_INPUT_ACQUIRING, OperationType.OPERATION_TYPE_INPUT_SECURITIES,
        OperationType.OPERATION_TYPE_OUTPUT, OperationType.OPERATION_TYPE_OUTPUT_ACQUIRING,
        OperationType.OPERATION_TYPE_OUTPUT_SECURITIES, OperationType.OPERATION_TYPE_OUTPUT_SWIFT
    );

    public static final Set<String> PAYMENT_TICKERS = Set.of(Constants.PAY_IN_RUB, Constants.PAY_IN_USD,
            Constants.PAY_IN_EUR, Constants.PAY_OUT_USD, Constants.PAY_OUT_RUB, Constants.PAY_OUT_EUR);
}
