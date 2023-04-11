package com.home.project.portfolio.model.operations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String tradeId;
    private ZonedDateTime date;
    private int quantity;
    private double price;
}
