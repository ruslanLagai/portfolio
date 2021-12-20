package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.RevenueCalculator;
import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.OperationGroups.TRADING_OPERATIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Class to test {@link RevenueProcessor}
 */
@ExtendWith(MockitoExtension.class)
class RevenueProcessorTest extends AbstractProcessorTest {

    private static final List<Operation> alexionOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000G30YX4"))
            .collect(Collectors.toList());
    private static final List<Operation> biomarinOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000CZWZ05"))
            .collect(Collectors.toList());
    private static final List<Operation> aaplOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000B9XRY4"))
            .collect(Collectors.toList());
    private static final List<Operation> sberOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG0047315Y7"))
            .collect(Collectors.toList());
    private static final List<Operation> amznOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000BVPV84"))
            .collect(Collectors.toList());

    private final RevenueCalculator calculator = new RevenueCalculator();
    private final RevenueProcessor processor = new RevenueProcessor(calculator);

    @DisplayName("test empty positions")
    @Test
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(ALEXION, alexionOps);
        map.put("BMRN", biomarinOps);

        var analyticDataList = processor.apply(map, Collections.emptyList());
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(ALEXION));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(19.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
        });
    }

    @DisplayName("test have positions")
    @Test
    void applyPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, aaplOps);
        map.put(SBERP, sberOps);
        var positions = TestUtils.readPositions()
                .getPayload().getPositions().stream()
                .filter(position -> position.getTicker().equals(AAPL) || position.getTicker().equals(SBERP))
                .collect(Collectors.toList());

        var analyticDataList = processor.apply(map, positions);
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(1182.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(1).getTicker(), Matchers.equalTo(SBERP));
            assertThat(analyticDataList.get(1).getRevenue(), Matchers.equalTo(1190.0));
            assertThat(analyticDataList.get(1).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(analyticDataList.get(1).getFigi(), Matchers.equalTo("BBG0047315Y7"));
        });
    }


    @DisplayName("test operations more than positions")
    @Test
    void testApplyPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, aaplOps);
        map.put(AMZN, amznOps);

        var analyticDataList = processor.apply(map, AAPL_POSITIONS);
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(1060.0));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(1).getTicker(), Matchers.equalTo(AMZN));
            assertThat(analyticDataList.get(1).getRevenue(), Matchers.greaterThan(537.0));
            assertThat(analyticDataList.get(1).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(1).getFigi(), Matchers.equalTo("BBG000BVPV84"));
        });
    }
}