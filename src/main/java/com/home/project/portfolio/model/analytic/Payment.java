package com.home.project.portfolio.model.analytic;

import com.home.project.portfolio.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OperationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Currency currency;
    private Double payment;
    private OperationType operationType;
}
