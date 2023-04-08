package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.Calculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.Payment;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.utils.OperationGroups;
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
 * Processor for payments (in/out)
 */
@Component
@Log4j2
public class PaymentProcessor implements AnalyticProcessor {

    private final Calculator paymentCalculator;

    public PaymentProcessor(Calculator paymentCalculator) {
        this.paymentCalculator = paymentCalculator;
    }

    @Override
    public List<AnalyticData> apply(MultiValueMap<String, Operation> operations,
                                    List<Position> positions, String accountId) {
        List<AnalyticData> analyticData = new ArrayList<>();
        log.info("Processing payments calculation");

        for (Map.Entry<String, List<Operation>> entry : operations.entrySet()) {
            if (entry.getKey() != null && OperationGroups.PAYMENT_TICKERS.contains(entry.getKey())) {
                analyticData.add(calculate(entry.getValue()));
            }
        }
        return analyticData;
    }

    private AnalyticData calculate(List<Operation> ops) {

        var payInUsd = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_INPUT, Currency.USD));
        var payInRub = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_INPUT, Currency.RUB));
        var payInEur = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_INPUT, Currency.EUR));
        log.info("Pay in: usd {}, rub {}, eur {}", payInUsd, payInRub, payInEur);

        var payOutUsd = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_OUTPUT, Currency.USD));
        var payOutRub = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_OUTPUT, Currency.RUB));
        var payOutEur = paymentCalculator.calculate(extractOperations(ops, OperationType.OPERATION_TYPE_OUTPUT, Currency.EUR));
        log.info("Computed pay out: usd {}, rub {}, eur {}", payOutUsd, payOutRub, payOutEur);

        var payments = Arrays.asList(
                buildPayment(payInUsd, OperationType.OPERATION_TYPE_INPUT, Currency.USD),
                buildPayment(payInRub, OperationType.OPERATION_TYPE_INPUT, Currency.RUB),
                buildPayment(payInEur, OperationType.OPERATION_TYPE_INPUT, Currency.EUR),
                buildPayment(payOutUsd, OperationType.OPERATION_TYPE_OUTPUT, Currency.USD),
                buildPayment(payOutRub, OperationType.OPERATION_TYPE_OUTPUT, Currency.RUB),
                buildPayment(payOutEur, OperationType.OPERATION_TYPE_OUTPUT, Currency.EUR)
        );
        return AnalyticData.builder()
                .payment(payments)
                .isPayment(true)
                .build();
    }

    private static Payment buildPayment(double pay, OperationType operationType,
                                        Currency currency) {
        return Payment.builder()
                .payment(pay)
                .operationType(operationType)
                .currency(currency)
                .build();
    }

    private static List<Operation> extractOperations(List<Operation> operations,
                                                     OperationType operationType, Currency currency) {
        return operations.stream()
                .filter(operation -> operation.getOperationType().equals(operationType))
                .filter(operation -> operation.getCurrency().equals(currency))
                .collect(Collectors.toList());
    }
}
