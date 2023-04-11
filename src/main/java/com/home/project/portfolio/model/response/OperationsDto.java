package com.home.project.portfolio.model.response;

import com.home.project.portfolio.model.operations.Operation;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dto to return
 */
@Getter
public class OperationsDto {


    private final MultiValueMap<String, Operation> operationsByTicker = new LinkedMultiValueMap<>();
    private final MultiValueMap<OperationType, Operation> commissions = new LinkedMultiValueMap<>();
    private final MultiValueMap<OperationType, Operation> payments = new LinkedMultiValueMap<>();
    private final MultiValueMap<OperationType, Operation> taxes = new LinkedMultiValueMap<>();

    public void addOperationOnStock(String ticker, Operation operation) {
        operationsByTicker.add(ticker, operation);
    }

    public void addCommission(Operation operation) {
        commissions.add(operation.getOperationType(), operation);
    }

    public void addPayment(Operation operation) {
        payments.add(operation.getOperationType(), operation);
    }

    public void addTax(Operation operation) {
        taxes.add(operation.getOperationType(), operation);
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
