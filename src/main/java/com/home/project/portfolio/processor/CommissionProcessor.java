package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.analytic.ServiceCommission;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Processor for stocks that currently present in portfolio
 */
@Component
@Log4j2
public class CommissionProcessor implements AnalyticProcessor {

    private static final Set<OperationType> SERVICE_COMMISSIONS = Set.of(OperationType.SERVICE_COMMISSION,
            OperationType.MARGIN_COMMISSION, OperationType.OTHER_COMMISSION, OperationType.EXCHANGE_COMMISSION);

    private static final BiFunction<Double, Operation, AnalyticData> STOCK_PROCESSOR =
            (commission, operation) -> AnalyticData.builder()
                    .ticker(operation.getTicker())
                    .figi(operation.getFigi())
                    .commission(commission)
                    .isCommission(true)
                    .instrumentType(operation.getInstrumentType())
                    .currency(operation.getCurrency())
                    .build();
    private static final BiFunction<Double, Operation, AnalyticData> SERVICE_COMMISSION_PROCESSOR =
            (commission, operation) -> AnalyticData.builder()
                    .ticker(operation.getTicker())
                    .isServiceCommission(true)
                    .serviceCommission(
                            ServiceCommission.builder()
                                    .currency(operation.getCurrency())
                                    .commission(commission)
                                    .build()
                    )
                    .build();

    private final Calculator commissionCalculator;

    public CommissionProcessor(Calculator commissionCalculator) {
        this.commissionCalculator = commissionCalculator;
    }

    @Override
    public List<AnalyticData> apply(MultiValueMap<String, Operation> operations,
                                    List<Position> positions) {
        List<AnalyticData> commission = new ArrayList<>();
        log.info("Processing commission calculation");
        operations.forEach((ticker, ops) -> {
            var commissions = ops.stream()
                    .filter(operation -> OperationGroups.COMMISSIONS.contains(operation.getOperationType()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(commissions)) {
                commission.add(calculate(ticker, commissions));
            }
        });
        return commission;
    }

    private AnalyticData calculate(String ticker, List<Operation> ops) {
        var operation = ops.iterator().next();

        //this is workaround as operations comes from finkoff without ticker
        operation.setTicker(ticker);

        var commission = commissionCalculator.calculate(ops);
        log.info("Computed commission for {}, commission {}", ticker, commission);
        return SERVICE_COMMISSIONS.contains(operation.getOperationType())
                ? SERVICE_COMMISSION_PROCESSOR.apply(commission, operation)
                : STOCK_PROCESSOR.apply(commission, operation);
    }
}
