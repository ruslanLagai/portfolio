package com.home.project.portfolio.model.operations;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Tinfoff api operation
 */
@Data
public class Operation {
    private long id;
    private Status status;
    private Currency currency;
    private double payment;
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
