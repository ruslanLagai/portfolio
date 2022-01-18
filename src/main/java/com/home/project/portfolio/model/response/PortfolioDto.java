package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Overbook;
import com.home.project.portfolio.model.portfolio.Distribution;
import com.home.project.portfolio.model.portfolio.Position;
import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * Class to return back to from end
 */
@Data
public class PortfolioDto {
    private List<Position> positions = new ArrayList<>();
    private Map<String, Overbook> prices = new HashMap<>();
    private Distribution distribution;
    private Map<Currency, CurrencyDto> cash = new HashMap<>();

    @Data
    @Builder
    public static class CurrencyDto {
        private double balance;
        private double currentPrice;
        private double averagePrice;
    }
}
