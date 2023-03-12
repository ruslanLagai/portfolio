package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Shows is stock is available for trading
 */
@AllArgsConstructor
@Getter
public enum StockAvailability {
    NOT_AVAILABLE("NotAvailableForTrading"),
    AVAILABLE("NormalTrading");

    private final String status;

    @JsonCreator
    public static StockAvailability parse(String value) {
        return Stream.of(values())
                .filter(period -> value.equalsIgnoreCase(period.getStatus()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
