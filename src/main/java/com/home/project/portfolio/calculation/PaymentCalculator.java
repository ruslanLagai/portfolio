package com.home.project.portfolio.calculation;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Class to calculate payment for instrument
 */
@Component
@Log4j2
public class PaymentCalculator implements Calculator {

    /**
     * @param operations - list of operations on particular ticker
     */
    @Override
    public double calculate(List<Operation> operations) {
        if (CollectionUtils.isEmpty(operations)) {
            return 0.0;
        }
        var total = operations.stream()
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .filter(operation -> OperationGroups.PAYMENTS.contains(operation.getOperationType()))
                .map(operation -> Math.abs(operation.getPayment()))
                .reduce(0.0, Double::sum);

        return Math.floor(total * 100) / 100;
    }
}
