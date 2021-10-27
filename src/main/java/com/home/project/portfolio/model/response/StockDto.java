package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.Currency;
import lombok.*;

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
public class StockDto {
    private String ticker;
    private String figi;
    private double revenue;
    private double commission;
    private Currency currency;
}
