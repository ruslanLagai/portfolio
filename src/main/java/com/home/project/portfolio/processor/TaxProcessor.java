package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.Payment;
import com.home.project.portfolio.model.analytic.Taxes;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.utils.Constants;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processor for taxes
 */
@Component
@Log4j2
public class TaxProcessor implements AnalyticProcessor {

    private final Calculator taxCalculator;

    public TaxProcessor(Calculator taxCalculator) {
        this.taxCalculator = taxCalculator;
    }

    @Override
    public List<AnalyticData> apply(MultiValueMap<String, Operation> operations,
                                    List<Position> positions) {
        AnalyticData analyticData = null;
        log.info("Processing taxes calculation");

        for (Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
            if (entry.getKey() != null && Constants.TAX_RUB.equals(entry.getKey())) {
                analyticData = calculate(entry.getValue());
            }
        }
        return analyticData != null ? Collections.singletonList(analyticData) : Collections.emptyList();
    }

    private AnalyticData calculate(List<Operation> ops) {

        var taxBack = taxCalculator.calculate(extractOperations(ops, OperationType.TAX_BACK));
        var taxDividend = taxCalculator.calculate(extractOperations(ops, OperationType.TAX_DIVIDEND));
        var taxCoupon = taxCalculator.calculate(extractOperations(ops, OperationType.TAX_COUPON));
        var taxPaid = taxCalculator.calculate(extractOperations(ops, OperationType.TAX));

        log.info("Tax paid {}, tax back {}, tax from dividend {}, tax from coupon {}",
                taxPaid, taxBack, taxDividend, taxCoupon);

        var taxes = Arrays.asList(
                buildTax(taxBack, OperationType.TAX_BACK),
                buildTax(taxPaid, OperationType.TAX),
                buildTax(taxDividend, OperationType.TAX_DIVIDEND),
                buildTax(taxCoupon, OperationType.TAX_COUPON)
        );

        return AnalyticData.builder()
                .taxes(taxes)
                .isTaxes(true)
                .ticker(Constants.TAX_RUB)
                .build();
    }

    private static Taxes buildTax(double pay, OperationType operationType) {
        return Taxes.builder()
                .taxes(pay)
                .operationType(operationType)
                .currency(Currency.RUB)
                .build();
    }

    private static List<Operation> extractOperations(List<Operation> operations, OperationType operationType) {
        return operations.stream()
                .filter(operation -> operation.getOperationType().equals(operationType))
                .collect(Collectors.toList());
    }
}
