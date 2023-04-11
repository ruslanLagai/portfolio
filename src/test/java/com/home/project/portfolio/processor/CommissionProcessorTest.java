package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.CommissionCalculator;
import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.utils.OperationGroups;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.util.Collections;
import java.util.List;

import static com.home.project.portfolio.utils.Constants.SERVICE_COMMISSION_RUB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Class to test {@link CommissionCalculator}
 */
@ExtendWith(MockitoExtension.class)
class CommissionProcessorTest extends AbstractProcessorTest {

    private static final List<Operation> COMMISSIONS_RUB = OPERATIONS
            .stream()
            .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
            .filter(operation -> !StringUtils.hasText(operation.getFigi()))
            .filter(operation -> OperationGroups.COMMISSIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getCurrency().equalsIgnoreCase(Currency.RUB.getCode()))
            .toList();
    private static final List<Operation> VKCO_OPS = OPERATIONS
            .stream()
            .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
            .filter(operation -> StringUtils.hasText(operation.getFigi()) && operation.getFigi().equals("BBG00178PGX3"))
            .toList();
    private static final List<Operation> AMZN_OPS = OPERATIONS
            .stream()
            .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
            .filter(operation -> StringUtils.hasText(operation.getFigi()) && operation.getFigi().equals("BBG000BVPV84"))
            .toList();

    CommissionCalculator calculator = new CommissionCalculator();
    CommissionProcessor processor = new CommissionProcessor(calculator);

    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Test
    @DisplayName("Calculate commission - VKCO")
    void apply() {
        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(VKCO, VKCO_OPS.stream().map(operation -> mapper.map(operation, VKCO)).toList());
        map.put(SERVICE_COMMISSION_RUB, COMMISSIONS_RUB.stream().map(operation -> mapper.map(operation, "")).toList());

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(2));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(VKCO));
            assertThat(result.get(0).getCommission(), Matchers.equalTo(280.15));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG00178PGX3"));
        });
        assertAll(() -> {
            assertThat(result.get(1).getServiceCommission().getCommission(), Matchers.equalTo(1495.8));
            assertThat(result.get(1).getServiceCommission().getCurrency(), Matchers.equalTo(Currency.RUB));
        });
    }

    @Test
    @DisplayName("Calculate commission - VKCO, AMZN")
    void applyTwoStocks() {
        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(VKCO, VKCO_OPS.stream().map(operation -> mapper.map(operation, VKCO)).toList());
        map.put(AMZN, AMZN_OPS.stream().map(operation -> mapper.map(operation, AMZN)).toList());

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(2));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(VKCO));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCommission(), Matchers.equalTo(280.15));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG00178PGX3"));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
        });

        assertAll(() -> {
            assertThat(result.get(1).getTicker(), Matchers.equalTo(AMZN));
            assertThat(result.get(1).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(1).getCommission(), Matchers.equalTo(3.13));
            assertThat(result.get(1).getFigi(), Matchers.equalTo("BBG000BVPV84"));
            assertThat(result.get(1).getCurrency(), Matchers.equalTo(Currency.USD));
        });
    }

    @Test
    @DisplayName("Calculate commission - no positions")
    void applyNoPositions() {
        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(VKCO, VKCO_OPS.stream().map(operation -> mapper.map(operation, VKCO)).toList());

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(VKCO));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCommission(), Matchers.greaterThan(7.0));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG00178PGX3"));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
        });
    }

}