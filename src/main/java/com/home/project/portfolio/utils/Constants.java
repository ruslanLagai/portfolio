package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.Currency;

import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Constants to use
 */
public class Constants {

    public static final String TINKOFF_API_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TINKOFF_API_DATE_TIME_FORMAT);
    public static final String NO_FIGI = "noFigi";
    public static final String SERVICE_COMMISSION_USD = "serviceCommissionUsd";
    public static final String SERVICE_COMMISSION_RUB = "serviceCommissionRub";
    public static final String PAY_IN_RUB = "payInRub";
    public static final String PAY_IN_USD = "payInUsd";
    public static final String PAY_IN_EUR = "payInEur";
    public static final String PAY_OUT_USD = "payOutUsd";
    public static final String PAY_OUT_RUB = "payOutRub";
    public static final String PAY_OUT_EUR = "payOutEur";
    public static final String TAX_RUB = "taxRub";

    public static final List<String> SPECIAL_TICKERS = Collections.unmodifiableList(Arrays.asList(
            SERVICE_COMMISSION_USD,
            SERVICE_COMMISSION_RUB,
            PAY_IN_RUB,
            PAY_IN_USD,
            PAY_IN_EUR,
            PAY_OUT_USD,
            PAY_OUT_RUB,
            PAY_OUT_EUR,
            TAX_RUB
    ));
    public static final Map<Currency, String> CURRENCY_COMMISSION_MAP = Map.of(
            Currency.RUB, SERVICE_COMMISSION_RUB,
            Currency.USD, SERVICE_COMMISSION_USD
    );
    public static final Map<Currency, String> CURRENCY_PAYMENT_MAP = Map.of(
            Currency.RUB, PAY_IN_RUB,
            Currency.USD, SERVICE_COMMISSION_USD
    );
    public static final Set<Period> PERIODS_TO_SEARCH_OLDER_OPERATIONS = new LinkedHashSet<>();

    static {
        PERIODS_TO_SEARCH_OLDER_OPERATIONS.add(Period.ofMonths(1));
        PERIODS_TO_SEARCH_OLDER_OPERATIONS.add(Period.ofMonths(6));
        PERIODS_TO_SEARCH_OLDER_OPERATIONS.add(Period.ofYears(1));
        PERIODS_TO_SEARCH_OLDER_OPERATIONS.add(Period.ofYears(3));
        PERIODS_TO_SEARCH_OLDER_OPERATIONS.add(Period.ofYears(5));

    }
}
