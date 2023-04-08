package com.home.project.portfolio.utils;

import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

/**
 * @author rlagay
 */
public class PriceUtils {

    public static double toDoubleValue(Quotation quotation) {
        return Double.parseDouble(quotation.getUnits() + "." + Math.abs(quotation.getNano()));
    }

    public static double toDoubleValue(MoneyValue moneyValue) {
        return Double.parseDouble(moneyValue.getUnits() + "." + Math.abs(moneyValue.getNano()));
    }
}
