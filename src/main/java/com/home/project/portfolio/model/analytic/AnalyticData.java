package com.home.project.portfolio.model.analytic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Class to store financial results on stock:
 * revenue
 * commission
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticData {
    private String ticker;
    private String figi;
    private double revenue;
    private double commission;
    private Currency currency;
    private double totalSoldSum;
    private double totalBoughtSum;
    private double revenuePercentage;
    private double dividend;
    private double coupons;
    @JsonIgnore
    private ServiceCommission serviceCommission;
    @JsonIgnore
    private List<Payment> payment;
    @JsonIgnore
    private List<Taxes> taxes;
    private boolean isCommission;
    private boolean isRevenue;
    private boolean isServiceCommission;
    private boolean isPayment;
    private boolean isTaxes;
    private InstrumentType instrumentType;

    public boolean isCommission() {
        return isCommission;
    }

    public boolean isRevenue() {
        return isRevenue;
    }

    public boolean isServiceCommission() {
        return isServiceCommission;
    }

    public boolean isPayment() {
        return isPayment;
    }

    public boolean isTaxes() {
        return isTaxes;
    }
}
