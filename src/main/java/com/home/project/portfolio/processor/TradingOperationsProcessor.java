package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.response.OperationsDto;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Class to process trading operations: buy, sell, etc
 */
@Component
@Log4j2
public class TradingOperationsProcessor implements OperationsProcessor {

    /**
     * Add trading operations:
     *
     * @param operations - operations for the period
     * @param operationsDto - operationDto to populate
     */
    @Override
    public void apply(List<Operation> operations, OperationsDto operationsDto) {
        operations.stream()
                .filter(operation -> OperationGroups.TRADING_OPERATIONS.contains(operation.getOperationType()))
                .forEach(operation -> operationsDto.addOperationOnStock(operation.getTicker(), operation));
    }
}
