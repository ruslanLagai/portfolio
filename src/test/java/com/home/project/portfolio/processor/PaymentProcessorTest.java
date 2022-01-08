package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.PaymentCalculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class to test {@link PaymentProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test calculate payments")
class PaymentProcessorTest extends AbstractProcessorTest {

    private static final List<Operation> PAY_IN_OPS = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getOperationType().equals(OperationType.PAY_IN))
            .collect(Collectors.toList());

    PaymentCalculator calculator = new PaymentCalculator();
    PaymentProcessor processor = new PaymentProcessor(calculator);

    @Test
    @DisplayName("All operations")
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("payInRub", ALL_OPERATIONS);

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getPayment().size(), Matchers.equalTo(6));

            assertThat(result.get(0).getPayment().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(2).getCurrency(), Matchers.equalTo(Currency.EUR));
            assertThat(result.get(0).getPayment().get(3).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(4).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(5).getCurrency(), Matchers.equalTo(Currency.EUR));

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));

            assertThat(result.get(0).getPayment().get(0).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(1).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(2).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(3).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(4).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(5).getPayment(), Matchers.equalTo(0.0));
        });
    }

    @Test
    @DisplayName("No payments")
    void applyTwoStocks() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("payInRub", AAPL_OPS);

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getPayment().size(), Matchers.equalTo(6));

            assertThat(result.get(0).getPayment().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(2).getCurrency(), Matchers.equalTo(Currency.EUR));
            assertThat(result.get(0).getPayment().get(3).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(4).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(5).getCurrency(), Matchers.equalTo(Currency.EUR));

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));

            assertThat(result.get(0).getPayment().get(0).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(1).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(2).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(3).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(4).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(5).getPayment(), Matchers.equalTo(0.0));

        });
    }

    @Test
    @DisplayName("Only pay in")
    void applyNoPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("payInRub", PAY_IN_OPS);

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getPayment().size(), Matchers.equalTo(6));

            assertThat(result.get(0).getPayment().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(2).getCurrency(), Matchers.equalTo(Currency.EUR));
            assertThat(result.get(0).getPayment().get(3).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(4).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(5).getCurrency(), Matchers.equalTo(Currency.EUR));

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.PAY_IN));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.PAY_OUT));

            assertThat(result.get(0).getPayment().get(0).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(1).getPayment(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getPayment().get(2).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(3).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(4).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(5).getPayment(), Matchers.equalTo(0.0));
        });
    }

}