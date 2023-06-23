package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
public class OperationUtils {

    public static List<Operation> sortBuyOperations(List<Operation> operations) {
        return operations.stream()
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .filter(operation -> OperationGroups.BUY_OPERATIONS.contains(operation.getOperationType()))
                .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public static int getFirstOwnedOperationIndex(Integer ownedSum, List<Operation> sortedOperations) {
        // if not enough operations -> all operations are not closed
        var totalExecuted = sortedOperations.stream()
                .map(Operation::getQuantityExecuted)
                .mapToInt(Integer::intValue)
                .sum();
        if (totalExecuted <= ownedSum) {
            return CollectionUtils.isEmpty(sortedOperations) ? -1 : sortedOperations.size();
        }

        int sum = 0;
        int index = -1;
        for (Operation sortedOperation : sortedOperations) {
            sum += sortedOperation.getQuantityExecuted();
            if (sum >= ownedSum) {
                index = sortedOperations.indexOf(sortedOperation) + 1;
                break;
            }
        }
        return index;
    }

    public static Operation buildOperation(Operation operation, int quantityToBeAdded) {
        // workaround for vtbr as price is > 10 times
        var payment = operation.getPrice() * quantityToBeAdded * Math.signum(operation.getPayment());
        return Operation.builder()
                .date(operation.getDate())
                .status(operation.getStatus())
                .operationType(operation.getOperationType())
                .id(operation.getId())
                .ticker(operation.getTicker())
                .figi(operation.getFigi())
                .instrumentType(operation.getInstrumentType())
                .isMarginCall(operation.isMarginCall())
                .trades(operation.getTrades())
                .commission(operation.getCommission())
                .price(operation.getPrice())
                .currency(operation.getCurrency())
                .quantityExecuted(quantityToBeAdded)
                .quantity(quantityToBeAdded)
                .payment("VTBR".equals(operation.getTicker()) ? payment / 10 : payment)
                .build();
    }
}
