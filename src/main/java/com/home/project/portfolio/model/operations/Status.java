package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Status of operation
 */
@AllArgsConstructor
@Getter
public enum Status {
    DONE("Done"),
    PROGRESS("Progress"),
    DECLINE("Decline");

    private final String status;

    @JsonCreator
    public static Status parse(String value) {
        return Stream.of(values())
                .filter(status -> value.equalsIgnoreCase(status.getStatus()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
