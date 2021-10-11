package com.home.project.portfolio.model.operations;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Tinfoff api operation
 */
@Data
public class Operation {
    private long id;
    private Status status;
    private Currency currency;
    private double payment;
    private String figi;
    private String ticker;
    private InstrumentType instrumentType;
    private boolean isMarginCall;
    private LocalDateTime date;
    private OperationType operationType;
}
