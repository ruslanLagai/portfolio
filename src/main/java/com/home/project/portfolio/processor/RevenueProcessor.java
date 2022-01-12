package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.service.OperationsService;
import com.home.project.portfolio.utils.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Constants.PERIODS_TO_SEARCH_OLDER_OPERATIONS;

/**
 * Revenue processor for stocks
 * trades
 * dividends
 */
@Component
@Log4j2
public class RevenueProcessor implements AnalyticProcessor {

    private final Calculator revenueCalculator;
    private final OperationsService operationsService;

    public RevenueProcessor(Calculator revenueCalculator, OperationsService operationsService) {
        this.revenueCalculator = revenueCalculator;
        this.operationsService = operationsService;
    }

    @Override
    public List<AnalyticData> apply(MultiValueMap<String, Operation> operations,
                                    List<Position> positions, String accountId) {
        List<AnalyticData> analyticDataList = new ArrayList<>();
        log.info("Processing revenue calculation for stocks in portfolio, stocks in portfolio {}",
                positions.size());

        // remove operations that are not closed
        removeOwnedOperations(operations, positions);

        for (Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
            var ticker = entry.getKey();
            var ops = entry.getValue();
            if (ops.iterator().hasNext() && ops.iterator().next().getInstrumentType() != null
                    && ops.iterator().next().getInstrumentType().equals(InstrumentType.CURRENCY)) {
                log.debug("Skipping currencies");
                continue;
            }

            var tradingOperations = ops.stream()
                    .filter(operation -> !Constants.SPECIAL_TICKERS.contains(operation.getTicker()))
                    .filter(operation -> operation.getFigi() != null)
                    .filter(operation -> operation.getOperationType().equals(OperationType.SELL)
                            || operation.getOperationType().equals(OperationType.BUY)
                            || operation.getOperationType().equals(OperationType.BUY_CARD))
                    .collect(Collectors.toList());

            // add operations if stock was bought/sold before requested period
            addOlderOperations(tradingOperations, accountId);

            var revenue = revenueCalculator.calculate(tradingOperations);
            log.info("Computed revenue for {}, revenue {}", ticker, revenue);

            var operation = tradingOperations.stream().findAny().orElse(new Operation());
            analyticDataList.add(AnalyticData.builder()
                    .figi(operation.getFigi())
                    .revenue(revenue)
                    .isRevenue(true)
                    .instrumentType(operation.getInstrumentType())
                    .currency(operation.getCurrency())
                    .ticker(ticker)
                    .build());
        }
        return analyticDataList;
    }

    /**
     * add operations if stock was bought/sold before requested period
     * @param operations - list of operations
     * @param accountId - account
     */
    private void addOlderOperations(List<Operation> operations, String accountId) {
        if (CollectionUtils.isEmpty(operations)) {
            return;
        }
        var boughtOperationsNum = operations.stream()
                .filter(operation -> operation.getOperationType().equals(OperationType.BUY)
                        || operation.getOperationType().equals(OperationType.BUY_CARD))
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .map(Operation::getQuantityExecuted)
                .reduce(0, Integer::sum);
        var soldOperationsNum = operations.stream()
                .filter(operation -> operation.getOperationType().equals(OperationType.SELL))
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .map(Operation::getQuantityExecuted)
                .reduce(0, Integer::sum);
        var difference = boughtOperationsNum - soldOperationsNum;

        // retrieving older operations is not needed
        if (difference == 0) {
            return;
        }

        var index = new AtomicInteger();
        log.info("Number of bought and sold operations is not equal, ticker {}, difference {}",
                operations.get(0).getTicker(), difference);

        operations.stream()
                .min(Comparator.comparing(Operation::getDate))
                .ifPresent(operation -> PERIODS_TO_SEARCH_OLDER_OPERATIONS.stream()
                        .takeWhile(op -> index.get() < Math.abs(difference))
                        .forEach(period -> {
                            if (difference > 0) {
                                operationsService.getLastOperationsForStock(operation.getDate().minus(period),
                                                operation.getDate().minusSeconds(30), operation.getFigi(), accountId)
                                        .stream()
                                        .filter(op -> op.getOperationType().equals(OperationType.SELL))
                                        .filter(op -> op.getStatus().equals(Status.DONE))
                                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                                        .takeWhile(op -> index.get() < Math.abs(difference))
                                        .forEach(op -> addOperation(operations, difference, index, op));
                            } else {
                                operationsService.getLastOperationsForStock(operation.getDate().minus(period),
                                                operation.getDate().minusSeconds(30), operation.getFigi(), accountId)
                                        .stream()
                                        .filter(op -> op.getOperationType().equals(OperationType.BUY) ||
                                                op.getOperationType().equals(OperationType.BUY_CARD))
                                        .filter(op -> op.getStatus().equals(Status.DONE))
                                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                                        .takeWhile(op -> index.get() < Math.abs(difference))
                                        .forEach(op -> addOperation(operations, difference, index, op));
                            }
                        }));
        log.info("Number of added trades from older period {}", index.get());
    }

    private void addOperation(List<Operation> operations, int difference, AtomicInteger index, Operation operation) {
        // processing case: bought 2 stocks in single operation, but difference is 1 stock
        var quantityToBeAdded = Math.abs(difference) - index.get();
        if (operation.getQuantityExecuted() > quantityToBeAdded) {
            operations.add(buildOperation(operation, quantityToBeAdded));
            index.addAndGet(quantityToBeAdded);
        } else {
            operations.add(operation);
            index.addAndGet(operation.getQuantityExecuted());
        }
    }

    private void removeOwnedOperations(MultiValueMap<String, Operation> operations,
                                                                   List<Position> positions) {
        var ownedTickers = positions.stream()
                .map(Position::getTicker)
                .collect(Collectors.toSet());
        operations.forEach((ticker, ops) -> {
            if (ownedTickers.contains(ticker)) {
                log.info("Processing owned stock, ticker {}", ticker);
                var ownedStocksNumber = positions.stream()
                        .filter(position -> position.getTicker().equals(ticker))
                        .filter(position -> position.getLots() != 0)
                        .map(Position::getBalance)
                        .map(Double::intValue)
                        .findFirst()
                        .orElse(0);
                log.debug("Owned stocks number {}, ticker {}", ownedStocksNumber, ticker);
                var sortedOperations = ops.stream()
                        .filter(operation -> operation.getStatus().equals(Status.DONE))
                        .filter(operation -> operation.getOperationType().equals(OperationType.BUY)
                                || operation.getOperationType().equals(OperationType.BUY_CARD))
                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                        .collect(Collectors.toList());

                var index = getFirstOwnedOperationIndex(ownedStocksNumber, sortedOperations);

                removeOwnedOperations(sortedOperations.subList(0, index != -1 ? index : 0), ops, ownedStocksNumber);
            }
        });
    }

    private void removeOwnedOperations(List<Operation> ownedOperations, List<Operation> allOperations,
                                       int ownedStocksNumber) {
        if (CollectionUtils.isEmpty(ownedOperations)) {
            return;
        }
        log.debug("Removing owned operations, ticker {}", ownedOperations.get(0).getTicker());
        var sum = 0;
        for (Operation operation : ownedOperations) {
            sum += operation.getQuantityExecuted();
            if (sum <= ownedStocksNumber) {
                allOperations.remove(operation);
                continue;
            }

            var splittedClosedOperationPart = buildOperation(operation, sum - ownedStocksNumber);

            var isDeleted = allOperations.remove(operation);
            allOperations.add(splittedClosedOperationPart);

            log.info("Owned operations quantityExecuted {}, ownedStocksNumber {}. " +
                    "\nCommon operations is removed: {}", sum, ownedStocksNumber, isDeleted);
            log.debug("Removed operation {}", operation.toString());
            log.debug("Added operation {}", splittedClosedOperationPart.toString());
        }
    }


    private Operation buildOperation(Operation operation, int quantityToBeAdded) {
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
                .payment(operation.getPrice() * quantityToBeAdded * Math.signum(operation.getPayment()))
                .build();
    }

    private int getFirstOwnedOperationIndex(Integer ownedStocksNumber, List<Operation> sortedOperations) {
        // if not enough operations -> all operations are not closed
        var totalExecuted = sortedOperations.stream()
                .map(Operation::getQuantityExecuted)
                .mapToInt(Integer::intValue)
                .sum();
        if (totalExecuted <= ownedStocksNumber) {
            return CollectionUtils.isEmpty(sortedOperations) ? -1 : sortedOperations.size();
        }

        int sum = 0;
        int index = -1;
        for (Operation sortedOperation : sortedOperations) {
            sum += sortedOperation.getQuantityExecuted();
            if (sum >= ownedStocksNumber) {
                index = sortedOperations.indexOf(sortedOperation) + 1;
                break;
            }
        }
        return index;
    }
}
