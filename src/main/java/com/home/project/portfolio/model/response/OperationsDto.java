package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.operations.Operation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dto to return
 */
public class OperationsDto {

    @Getter
    @Setter
    private MultiValueMap<String, Operation> operationsByTicker = new LinkedMultiValueMap<>();

    public void addOperationOnStock(String ticker, Operation operation) {
        operationsByTicker.add(ticker, operation);
    }

    public void addOperations(String ticker, List<Operation> operations) {
        operationsByTicker.addAll(ticker, operations);
    }

    public void sortOperationsByDate() {
        operationsByTicker.forEach((k, v) ->
                operationsByTicker.replace(k, v.stream()
                        .sorted(Comparator.comparing(Operation::getDate))
                        .collect(Collectors.toList())));
    }
}
