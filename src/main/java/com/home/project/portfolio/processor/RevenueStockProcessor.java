package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.StockDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Processor for stocks that currently present in portfolio
 */
@Component
@Log4j2
public class RevenueStockProcessor implements AnalyticProcessor {

    private final Calculator revenueCalculator;

    public RevenueStockProcessor(Calculator revenueCalculator) {
        this.revenueCalculator = revenueCalculator;
    }

    @Override
    public Map<String, StockDto> apply(MultiValueMap<String, Operation> operations,
                                       List<Position> positions) {
        Map<String, StockDto> stockAnalysis = new HashMap<>();
        log.info("Processing revenue calculation for stocks in portfolio, stocks in portfolio {}",
                positions.size());
        var ownedOperations = extractOwnedOperations(operations, positions);
        operations.forEach((ticker, ops) -> {
            if (!CollectionUtils.isEmpty(ownedOperations.get(ticker))) {
                ops.removeAll(ownedOperations.get(ticker));
                log.debug("Removed {} owned operations, ticker {}", ownedOperations.get(ticker).size(),
                        ticker);
            }
            var result = calculate(ticker, ops);

            if (stockAnalysis.containsKey(ticker)) {
                stockAnalysis.get(ticker).setRevenue(result.getRevenue());
            } else {
                stockAnalysis.put(ticker, result);
            }
        });
        return stockAnalysis;
    }

    private StockDto calculate(String ticker, List<Operation> ops) {
        var operation = ops.iterator().next();
        var revenue = revenueCalculator.calculateRevenue(ops);
        log.info("Computed revenue for {}, revenue {}", ticker, revenue);
        return StockDto.builder()
                .ticker(ticker)
                .figi(operation.getFigi())
                .revenue(revenue)
                .currency(operation.getCurrency())
                .build();
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
