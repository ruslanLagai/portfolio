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
import org.springframework.util.LinkedMultiValueMap;
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

        // extracting operations that are not closed
        var ownedOperations = extractOwnedOperations(operations, positions);

        for (Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
            var ticker = entry.getKey();
            var ops = entry.getValue();
            if (ops.iterator().hasNext() && ops.iterator().next().getInstrumentType() != null
                    && ops.iterator().next().getInstrumentType().equals(InstrumentType.CURRENCY)) {
                log.debug("Skipping currencies");
                continue;
            }
            if (!CollectionUtils.isEmpty(ownedOperations.get(ticker))) {
                ops.removeAll(ownedOperations.get(ticker));
                log.debug("Removed {} owned operations, ticker {}", ownedOperations.get(ticker).size(),
                        ticker);
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
        var initialSize = operations.size();
        log.info("Number of bought and sold operations is not equal, ticker {}, difference {}",
                operations.get(0).getTicker(), difference);

        operations.stream()
                .min(Comparator.comparing(Operation::getDate))
                .ifPresent(operation -> PERIODS_TO_SEARCH_OLDER_OPERATIONS.stream()
                        .takeWhile(op -> operations.size() == initialSize)
                        .forEach(period -> {
                            var index = new AtomicInteger();
                            if (difference > 0) {
                                operationsService.getLastOperationsForStock(operation.getDate().minus(period),
                                                operation.getDate().minusSeconds(30), operation.getFigi(), accountId)
                                        .stream()
                                        .filter(op -> op.getOperationType().equals(OperationType.SELL))
                                        .filter(op -> op.getStatus().equals(Status.DONE))
                                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                                        .takeWhile(op -> index.get() < Math.abs(difference))
                                        .forEach(op -> {
                                            operations.add(op);
                                            index.addAndGet(op.getQuantityExecuted());
                                        });
                            } else {
                                operationsService.getLastOperationsForStock(operation.getDate().minus(period),
                                                operation.getDate().minusSeconds(30), operation.getFigi(), accountId)
                                        .stream()
                                        .filter(op -> op.getOperationType().equals(OperationType.BUY) ||
                                                op.getOperationType().equals(OperationType.BUY_CARD))
                                        .filter(op -> op.getStatus().equals(Status.DONE))
                                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                                        .takeWhile(op -> index.get() < Math.abs(difference))
                                        .forEach(op -> {
                                            operations.add(op);
                                            index.addAndGet(op.getQuantityExecuted());
                                        });
                            }
                        }));
        log.info("Number of added operations from older period {}", operations.size() - initialSize);
    }

    private MultiValueMap<String, Operation> extractOwnedOperations(MultiValueMap<String, Operation> operations,
                                                                    List<Position> positions) {
        MultiValueMap<String, Operation> ownedOperations = new LinkedMultiValueMap<>();
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
                ownedOperations.addAll(ticker, sortedOperations.subList(0, index != -1 ? index + 1 : 0));

            }
        });
        return ownedOperations;
    }

    private int getFirstOwnedOperationIndex(Integer ownedStocksNumber, List<Operation> sortedOperations) {
        int sum = 0;
        int index = -1;
        for (Operation sortedOperation : sortedOperations) {
            sum += sortedOperation.getQuantityExecuted();
            if (sum == ownedStocksNumber) {
                index = sortedOperations.indexOf(sortedOperation);
                break;
            }
            if (sum > ownedStocksNumber) {
                log.error("Incorrect number of owned positions");
                break;
            }
        }
        return index;
    }
}
