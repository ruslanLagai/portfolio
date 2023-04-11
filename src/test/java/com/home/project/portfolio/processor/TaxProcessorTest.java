package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.TaxCalculator;
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
 * class to test {@link TaxProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test taxes processor")
class TaxProcessorTest extends AbstractProcessorTest {

    private static final List<ru.tinkoff.piapi.contract.v1.Operation> VKCO_OPS = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> StringUtils.hasText(operation.getFigi()) && operation.getFigi().equals("BBG00178PGX3"))
        .toList();

    private final TaxCalculator taxCalculator = new TaxCalculator();
    private final TaxProcessor taxProcessor = new TaxProcessor(taxCalculator);
    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Test
    @DisplayName("All operations")
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("taxRub", OPERATIONS.stream().map(operation -> mapper.map(operation, "")).toList());

        var result = taxProcessor.apply(map, Collections.emptyList(), "");

        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getTaxes().size(), Matchers.equalTo(5));

            assertThat(result.get(0).getTaxes().get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(2).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(3).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(4).getCurrency(), Matchers.equalTo(Currency.RUB));

            assertThat(result.get(0).getTaxes().get(0).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_TAX_CORRECTION));
            assertThat(result.get(0).getTaxes().get(1).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_TAX));
            assertThat(result.get(0).getTaxes().get(2).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_DIVIDEND_TAX));
            assertThat(result.get(0).getTaxes().get(3).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON));
            assertThat(result.get(0).getTaxes().get(4).getOperationType(), Matchers.equalTo(OperationType.OPERATION_TYPE_BOND_TAX));

            assertThat(result.get(0).getTaxes().get(0).getTaxes(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getTaxes().get(1).getTaxes(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getTaxes().get(2).getTaxes(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getTaxes().get(3).getTaxes(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getTaxes().get(4).getTaxes(), Matchers.equalTo(0.0));

        });
    }

    @Test
    @DisplayName("No taxes")
    void applyTwoStocks() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(VKCO, VKCO_OPS.stream().map(operation -> mapper.map(operation, VKCO)).toList());

        var result = taxProcessor.apply(map, Collections.emptyList(), "");

        assertThat(result.size(), Matchers.equalTo(0));
    }
}