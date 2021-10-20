package com.home.project.portfolio.model.operations;

import com.home.project.portfolio.model.Currency;
import lombok.Data;

@Data
public class Commission {
    private Currency currency;
    private double value;
}
