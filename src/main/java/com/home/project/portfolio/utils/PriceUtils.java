package com.home.project.portfolio.utils;

import ru.tinkoff.piapi.contract.v1.Quotation;

/**
 * @author rlagay
 */
public class PriceUtils {

    public static double toDoubleValue(Quotation quotation) {
        return Double.parseDouble(quotation.getUnits() + "." + quotation.getNano());
    }
}
