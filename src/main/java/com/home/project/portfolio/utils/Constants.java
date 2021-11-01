package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.Currency;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants to use
 */
public class Constants {

    public static final String TINKOFF_API_DATE_TIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ssXXX";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TINKOFF_API_DATE_TIME_FORMAT);
    public static final String NO_FIGI = "noFigi";
    public static final String SERVICE_COMMISSION_USD = "serviceCommissionUsd";
    public static final String SERVICE_COMMISSION_RUB = "serviceCommissionRub";
    public static final Map<Currency, String> CURRENCY_STRING_MAP = new HashMap<>();

    static {
        CURRENCY_STRING_MAP.put(Currency.RUB, SERVICE_COMMISSION_RUB);
        CURRENCY_STRING_MAP.put(Currency.USD, SERVICE_COMMISSION_USD);
    }
}
