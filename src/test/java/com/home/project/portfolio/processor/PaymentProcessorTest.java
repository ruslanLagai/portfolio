package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.PaymentCalculator;
import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Class to test {@link PaymentProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test calculate payments")
class PaymentProcessorTest extends AbstractProcessorTest {

    private static final List<ru.tinkoff.piapi.contract.v1.Operation> PAY_IN_OPS = OPERATIONS
        .stream()
        .filter(operation -> operation.getOperationType().equals(ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_INPUT))
        .toList();
    private static final List<ru.tinkoff.piapi.contract.v1.Operation> VKCO_OPS = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> StringUtils.hasText(operation.getFigi()) && operation.getFigi().equals("BBG00178PGX3"))
        .toList();

    PaymentCalculator calculator = new PaymentCalculator();
    PaymentProcessor processor = new PaymentProcessor(calculator);
    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Test
    @DisplayName("All operations")
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("payInRub", OPERATIONS.stream().map(operation -> mapper.map(operation, "")).toList());

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

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));

            assertThat(result.get(0).getPayment().get(0).getPayment(), Matchers.equalTo(100.0));
            assertThat(result.get(0).getPayment().get(1).getPayment(), Matchers.equalTo(20000.0));
            assertThat(result.get(0).getPayment().get(2).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(3).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(4).getPayment(), Matchers.equalTo(5000.0));
            assertThat(result.get(0).getPayment().get(5).getPayment(), Matchers.equalTo(0.0));
        });
    }

    @Test
    @DisplayName("No payments")
    void applyTwoStocks() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("payInRub", VKCO_OPS.stream().map(operation -> mapper.map(operation, VKCO)).toList());

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

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));

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
        map.put("payInRub", PAY_IN_OPS.stream()
            .filter(operation -> operation.getCurrency().equals("rub"))
            .map(operation -> mapper.map(operation, "")).toList());
        map.put("payInUsd", PAY_IN_OPS.stream()
            .filter(operation -> operation.getCurrency().equals("usd"))
            .map(operation -> mapper.map(operation, "")).toList());

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(2));
            assertThat(result.get(0).getPayment().size(), Matchers.equalTo(6));

            assertThat(result.get(0).getPayment().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(2).getCurrency(), Matchers.equalTo(Currency.EUR));
            assertThat(result.get(0).getPayment().get(3).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getPayment().get(4).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getPayment().get(5).getCurrency(), Matchers.equalTo(Currency.EUR));

            assertThat(result.get(0).getPayment().get(0).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(1).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(2).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_INPUT));
            assertThat(result.get(0).getPayment().get(3).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(4).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));
            assertThat(result.get(0).getPayment().get(5).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_OUTPUT));

            assertThat(result.get(1).getPayment().get(0).getPayment(), Matchers.equalTo(100.0));
            assertThat(result.get(0).getPayment().get(1).getPayment(), Matchers.equalTo(20000.0));
            assertThat(result.get(0).getPayment().get(2).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(3).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(4).getPayment(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getPayment().get(5).getPayment(), Matchers.equalTo(0.0));
        });
    }

}