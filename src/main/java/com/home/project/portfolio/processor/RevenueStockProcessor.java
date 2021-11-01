package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.portfolio.Position;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Revenue processor for stocks
 *  trades
 *  dividends
 */
@Component
@Log4j2
public class RevenueStockProcessor implements AnalyticProcessor {

    private final Calculator revenueCalculator;

    public RevenueStockProcessor(Calculator revenueCalculator) {
        this.revenueCalculator = revenueCalculator;
    }

    @Override
    public List<AnalyticData> apply(MultiValueMap<String, Operation> operations,
                                    List<Position> positions) {
        List<AnalyticData> analyticDataList = new ArrayList<>();
        log.info("Processing revenue calculation for stocks in portfolio, stocks in portfolio {}",
                positions.size());
        var ownedOperations = extractOwnedOperations(operations, positions);
        operations.forEach((ticker, ops) -> {
            if (!CollectionUtils.isEmpty(ownedOperations.get(ticker))) {
                ops.removeAll(ownedOperations.get(ticker));
                log.debug("Removed {} owned operations, ticker {}", ownedOperations.get(ticker).size(),
                        ticker);
            }

            var operation = ops.iterator().next();
            var revenue = revenueCalculator.calculate(ops);
            log.info("Computed revenue for {}, revenue {}", ticker, revenue);

            analyticDataList.add(AnalyticData.builder()
                    .figi(operation.getFigi())
                    .revenue(revenue)
                    .isRevenue(true)
                    .currency(operation.getCurrency())
                    .ticker(ticker)
                    .build());
        });
        return analyticDataList;
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
                        .map(Position::getLots)
                        .findFirst()
                        .orElse(0);
                log.debug("Owned stocks number {}, ticker {}", ownedStocksNumber, ticker);
                var sortedOperations = ops.stream()
                        .filter(operation -> operation.getStatus().equals(Status.DONE))
                        .filter(operation -> operation.getOperationType().equals(OperationType.BUY)
                                || operation.getOperationType().equals(OperationType.BUY_CARD))
                        .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                        .collect(Collectors.toList());
                ownedOperations.addAll(ticker, sortedOperations.subList(0, ownedStocksNumber));
            }
        });
        return ownedOperations;
    }
}
