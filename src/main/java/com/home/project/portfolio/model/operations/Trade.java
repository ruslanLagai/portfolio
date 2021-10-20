package com.home.project.portfolio.model.operations;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Trade {
    private String tradeId;
    private ZonedDateTime date;
    private int quantity;
    private double price;
}
