package com.home.project.portfolio.model.operations;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Tinfoff api operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {
    private String id;
    private Status status;
    private Currency currency;
    private double payment;
    private double price;
    private int quantity;
    private int quantityExecuted;
    private String figi;
    private String ticker;
    private InstrumentType instrumentType;
    private boolean isMarginCall;
    private ZonedDateTime date;
    private OperationType operationType;
    private List<Trade> trades;
    private Commission commission;
}
