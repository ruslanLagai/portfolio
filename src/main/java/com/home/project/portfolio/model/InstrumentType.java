package com.home.project.portfolio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public enum InstrumentType {
    STOCK("Share"),
    ETF("Etf"),
    BOND("Bond"),
    FUTURES("Futures"),
    CURRENCY("Currency"),
    UNKNOWN("");

    @Setter
    private String type;

    @JsonCreator
    public static InstrumentType parse(String value) {
        return Stream.of(values())
            .filter(period -> value.equalsIgnoreCase(period.getType()))
            .findFirst()
            .orElseGet(() -> {
                log.warn("Unknown instrument type: {}", value);
                var type = UNKNOWN;
                type.setType(value);
                return type;
            });
    }
}
