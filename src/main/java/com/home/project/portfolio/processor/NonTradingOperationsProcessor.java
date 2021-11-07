package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.response.OperationsDto;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Class to process non trading operations
 */
@Component
@Log4j2
public class NonTradingOperationsProcessor implements OperationsProcessor {

    private static final BiFunction<Operation, OperationsDto,  String> ADD_COMMISSION = (operation, operationsDto) -> {
        log.debug("Adding commissions");
        operationsDto.addCommission(operation);
        return null;
    };
    private static final BiFunction<Operation, OperationsDto,  String> ADD_PAYMENT = (operation, operationsDto) -> {
        log.debug("Adding pay in & out");
        operationsDto.addPayment(operation);
        return null;
    };
    private static final BiFunction<Operation, OperationsDto,  String> ADD_TAX = (operation, operationsDto) -> {
        log.debug("Adding taxes");
        operationsDto.addTax(operation);
        return null;
    };
    private static final Map<OperationType, BiFunction<Operation, OperationsDto,  String>>
            OPERATION_TYPE_PROCESSORS = new HashMap<>();
    private static final Set<OperationType> NON_TRADING_OPERATIONS = new HashSet<>();

    static {
        NON_TRADING_OPERATIONS.addAll(OperationGroups.TAXES);
        NON_TRADING_OPERATIONS.addAll(OperationGroups.PAYMENTS);
        NON_TRADING_OPERATIONS.addAll(OperationGroups.COMMISSIONS);
        OperationGroups.TAXES.forEach(operationType -> OPERATION_TYPE_PROCESSORS.put(operationType, ADD_TAX));
        OperationGroups.COMMISSIONS.forEach(operationType -> OPERATION_TYPE_PROCESSORS.put(operationType, ADD_COMMISSION));
        OperationGroups.PAYMENTS.forEach(operationType -> OPERATION_TYPE_PROCESSORS.put(operationType, ADD_PAYMENT));
    }

    /**
     * Add interesting for user non trading operations:
     *  pay in/out
     *  commission
     *  taxes
     * @param operations - operations for the period
     * @param operationsDto - operationDto to populate
     */
    @Override
    public void apply(List<Operation> operations, OperationsDto operationsDto) {
        operations.stream()
                .filter(operation -> NON_TRADING_OPERATIONS.contains(operation.getOperationType()))
                .forEach(operation -> OPERATION_TYPE_PROCESSORS.get(operation.getOperationType())
                        .apply(operation, operationsDto));
    }
}
