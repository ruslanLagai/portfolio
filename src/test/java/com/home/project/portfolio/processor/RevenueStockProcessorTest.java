package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.RevenueCalculator;
import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.response.StockDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.OperationGroups.TRADING_OPERATIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link RevenueStockProcessor}
 */
@ExtendWith(MockitoExtension.class)
class RevenueStockProcessorTest {

    private static final String AAPL = "AAPL";
    private static final String SBERP = "SBERP";
    private static final String AMZN = "AMZN";
    public static final String ALEXION = "ALEXION";
    private final RevenueCalculator calculator = new RevenueCalculator();
    private final RevenueStockProcessor processor = new RevenueStockProcessor(calculator);
    private static final Operations OPERATIONS = TestUtils.readOperations();

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

    @DisplayName("test empty positions")
    @Test
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(ALEXION, alexionOps);
        map.put("BMRN", biomarinOps);

        Map<String, StockDto> stringStockDtoMap = processor.apply(map, Collections.emptyList());
        assertAll(() -> {
            assertFalse(stringStockDtoMap.isEmpty());
            assertThat(stringStockDtoMap.get(ALEXION).getTicker(), Matchers.equalTo(ALEXION));
            assertThat(stringStockDtoMap.get(ALEXION).getRevenue(), Matchers.greaterThan(0.0));
            assertThat(stringStockDtoMap.get(ALEXION).getCommission(), Matchers.equalTo(0.0));
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

        Map<String, StockDto> stringStockDtoMap = processor.apply(map, positions);
        assertAll(() -> {
            assertFalse(stringStockDtoMap.isEmpty());
            assertThat(stringStockDtoMap.get(AAPL).getTicker(), Matchers.equalTo(AAPL));
            assertThat(stringStockDtoMap.get(AAPL).getRevenue(), Matchers.greaterThan(0.0));
            assertThat(stringStockDtoMap.get(AAPL).getCommission(), Matchers.equalTo(0.0));
            assertThat(stringStockDtoMap.get(AAPL).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        assertAll(() -> {
            assertFalse(stringStockDtoMap.isEmpty());
            assertThat(stringStockDtoMap.get(SBERP).getTicker(), Matchers.equalTo(SBERP));
            assertThat(stringStockDtoMap.get(SBERP).getRevenue(), Matchers.greaterThan(0.0));
            assertThat(stringStockDtoMap.get(SBERP).getCommission(), Matchers.equalTo(0.0));
            assertThat(stringStockDtoMap.get(SBERP).getFigi(), Matchers.equalTo("BBG0047315Y7"));

        });
    }


    @DisplayName("test operations more than positions")
    @Test
    void testApplyPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, aaplOps);
        map.put(AMZN, amznOps);
        var positions = TestUtils.readPositions()
                .getPayload().getPositions().stream()
                .filter(position -> position.getTicker().equals(AAPL))
                .collect(Collectors.toList());

        Map<String, StockDto> stringStockDtoMap = processor.apply(map, positions);
        assertAll(() -> {
            assertFalse(stringStockDtoMap.isEmpty());
            assertThat(stringStockDtoMap.get(AAPL).getTicker(), Matchers.equalTo(AAPL));
            assertThat(stringStockDtoMap.get(AAPL).getRevenue(), Matchers.greaterThan(0.0));
            assertThat(stringStockDtoMap.get(AAPL).getCommission(), Matchers.equalTo(0.0));
            assertThat(stringStockDtoMap.get(AAPL).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        assertAll(() -> {
            assertFalse(stringStockDtoMap.isEmpty());
            assertThat(stringStockDtoMap.get(AMZN).getTicker(), Matchers.equalTo(AMZN));
            assertThat(stringStockDtoMap.get(AMZN).getRevenue(), Matchers.greaterThan(0.0));
            assertThat(stringStockDtoMap.get(AMZN).getCommission(), Matchers.equalTo(0.0));
            assertThat(stringStockDtoMap.get(AMZN).getFigi(), Matchers.equalTo("BBG000BVPV84"));

        });
    }
}