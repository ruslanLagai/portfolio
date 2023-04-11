package com.home.project.portfolio.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum Currency {
    RUB("rub"),
    USD("usd"),
    EUR("eur"),
    HKD("hkd"),
    CNY("cny"),
    UNKNOWN("unknown");

    private final String code;

    @JsonCreator
    public static Currency parse(String value) {
        return Stream.of(values())
            .filter(period -> value.equalsIgnoreCase(period.getCode()))
            .findFirst().orElse(UNKNOWN);
    }
}
