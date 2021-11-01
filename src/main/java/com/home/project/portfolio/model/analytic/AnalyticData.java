package com.home.project.portfolio.model.analytic;

import com.home.project.portfolio.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class to store financial results on stock:
 *  revenue
 *  commission
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticData {
    private String ticker;
    private String figi;
    private double revenue;
    private double commission;
    private Currency currency;
    private ServiceCommission serviceCommission;
    private boolean isCommission;
    private boolean isRevenue;

    public boolean isCommission() {
        return isCommission;
    }

    public boolean isRevenue() {
        return isRevenue;
    }


}
