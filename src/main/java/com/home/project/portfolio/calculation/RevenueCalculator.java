package com.home.project.portfolio.calculation;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Class to calculate revenue for stock
 */
@Component
@Log4j2
public class RevenueCalculator implements Calculator {

    /**
     *
     * @param operations - list of operations on particular ticker
     */
    @Override
    public double calculate(List<Operation> operations) {
        if (CollectionUtils.isEmpty(operations)) {
            log.warn("Empty or null list of operations");
            return 0.0;
        }
        var bought = operations.stream()
                .filter(operation -> operation.getOperationType().equals(OperationType.BUY)
                        || operation.getOperationType().equals(OperationType.BUY_CARD))
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .map(operation -> Math.abs(operation.getPayment()))
                .reduce(0.0, Double::sum);
        var sold = operations.stream()
                .filter(operation -> operation.getOperationType().equals(OperationType.SELL))
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .map(operation -> Math.abs(operation.getPayment()))
                .reduce(0.0, Double::sum);
        var diff = sold - bought;

        return Precision.round(diff, 2);
    }
}
