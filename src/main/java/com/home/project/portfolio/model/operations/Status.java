package com.home.project.portfolio.model.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.util.stream.Stream;

/**
 * Status of operation
 */
@AllArgsConstructor
@Getter
public enum Status {
    OPERATION_STATE_UNSPECIFIED("OPERATION_STATE_UNSPECIFIED"),
    UNRECOGNIZED("UNRECOGNIZED"),
    DONE("OPERATION_STATE_EXECUTED"),
    PROGRESS("OPERATION_STATE_PROGRESS"),
    DECLINE("OPERATION_STATE_CANCELED");

    private final String status;

    @JsonCreator
    public static Status parse(String value) {
        return Stream.of(values())
                .filter(status -> value.equalsIgnoreCase(status.getStatus()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public static Status parse(OperationState state) {
        return Stream.of(values())
            .filter(status -> state.name().equalsIgnoreCase(status.getStatus()))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
