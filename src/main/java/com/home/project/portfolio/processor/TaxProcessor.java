package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.Taxes;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.utils.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.util.ArrayList;
import java.util.Arrays;
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
                                    List<Position> positions, String accountId) {
        List<AnalyticData> analyticData = new ArrayList<>();
        log.info("Processing taxes calculation");

        for (Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
            calculate(entry.getValue(), analyticData);
        }
        return analyticData;
    }

    private void calculate(List<Operation> ops, List<AnalyticData> analyticData) {

        var taxBack = taxCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_TAX_CORRECTION));
        var taxDividend = taxCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_DIVIDEND_TAX));
        var taxCoupon = taxCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON));
        var taxPaid = taxCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_TAX));
        var boundTax = taxCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_BOND_TAX));

        if (taxBack == 0.0 && taxDividend == 0.0 && taxCoupon == 0.0 && taxPaid == 0.0) {
            return;
        }
        log.info("Tax paid {}, tax back {}, tax from dividend {}, tax from coupon {}",
                taxPaid, taxBack, taxDividend, taxCoupon);

        var taxes = Arrays.asList(
                buildTax(taxBack, OperationType.OPERATION_TYPE_TAX_CORRECTION),
                buildTax(taxPaid, OperationType.OPERATION_TYPE_TAX),
                buildTax(taxDividend, OperationType.OPERATION_TYPE_DIVIDEND_TAX),
                buildTax(taxCoupon, OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON),
                buildTax(boundTax, OperationType.OPERATION_TYPE_BOND_TAX)
        );

        analyticData.add(AnalyticData.builder()
                .taxes(taxes)
                .isTaxes(true)
                .ticker(Constants.TAX_RUB)
                .build());
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
                .filter(operation -> operation.getStatus().equals(Status.DONE))
                .collect(Collectors.toList());
    }
}
