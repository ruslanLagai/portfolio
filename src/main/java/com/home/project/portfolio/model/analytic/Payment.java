package com.home.project.portfolio.model.analytic;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Currency currency;
    private Double payment;
    private OperationType operationType;
}
