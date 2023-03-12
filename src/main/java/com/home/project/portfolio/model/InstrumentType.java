package com.home.project.portfolio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum InstrumentType {
    STOCK("Share"),
    ETF("Etf"),
    BOND("Bond"),
    FUTURES("Future"),
    CURRENCY("Currency");

    private final String type;

    @JsonCreator
    public static InstrumentType parse(String value) {
        return Stream.of(values())
                .filter(period -> value.equalsIgnoreCase(period.getType()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
