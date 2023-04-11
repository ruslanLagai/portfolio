package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.RevenueCalculator;
import com.home.project.portfolio.mapper.OperationMapper;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.service.OperationsService;
import com.home.project.portfolio.utils.Constants;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.home.project.portfolio.utils.OperationGroups.TRADING_OPERATIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link RevenueProcessor}
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class RevenueProcessorTest extends AbstractProcessorTest {

    private final List<Operation> numOps = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
        .filter(operation -> operation.getFigi().equals("BBG000BLKK03")).toList();
    private final List<Operation> vloOps = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
        .filter(operation -> operation.getFigi().equals("BBG000BBGGQ1")).toList();
    private final List<Operation> nlmkOps = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
        .filter(operation -> operation.getFigi().equals("BBG004S681B4")).toList();
    private final List<Operation> sberOps = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
        .filter(operation -> operation.getFigi().equals("BBG004730N88"))
        .toList();
    private final List<Operation> amznOps = OPERATIONS
        .stream()
        .filter(operation -> operation.getState().equals(OperationState.OPERATION_STATE_EXECUTED))
        .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
        .filter(operation -> operation.getFigi().equals("BBG000BVPV84")).toList();

    private final OperationsService operationsService = mock(OperationsService.class);
    private final RevenueCalculator calculator = new RevenueCalculator();
    private final RevenueProcessor processor = new RevenueProcessor(calculator, operationsService);
    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @DisplayName("test empty positions")
    @Test
    void a1apply() {
        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(HUMANA, numOps.stream().map(operation -> mapper.map(operation, HUMANA)).toList());
        map.put(VLO, vloOps.stream().map(operation -> mapper.map(operation, VLO)).toList());

        var analyticDataList = processor.apply(map, Collections.emptyList(), "1");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(HUMANA));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.equalTo(-4.56));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
        });
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(1).getTicker(), Matchers.equalTo(VLO));
            assertThat(analyticDataList.get(1).getRevenue(), Matchers.equalTo(-26.42));
            assertThat(analyticDataList.get(1).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(1).getCommission(), Matchers.equalTo(0.0));
        });
        verify(operationsService, times(0)).getLastOperationsForStock(any(), any(), any(), any());
    }

    @DisplayName("test have positions")
    @Test
    void a2applyPositions() {
        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(NLMK, new ArrayList<>(nlmkOps.stream().map(operation -> mapper.map(operation, NLMK)).toList()));
        map.put(SBER, new ArrayList<>(sberOps.stream().map(operation -> mapper.map(operation, SBER)).toList()));
        var positions = List.of(
            Position.builder()
                .figi("BBG004730N88")
                .ticker(SBER)
                .balance(10)
                .lots(1)
                .build()
        );

        var analyticDataList = processor.apply(map, positions, "2");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(NLMK));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.equalTo(-505.38));
            assertThat(analyticDataList.get(0).getRevenuePercentage(), Matchers.equalTo(-0.15));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG004S681B4"));
        });
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(1).getTicker(), Matchers.equalTo(SBER));
            assertThat(analyticDataList.get(1).getRevenue(), Matchers.equalTo(1109.6));
            assertThat(analyticDataList.get(1).getRevenuePercentage(), Matchers.equalTo(0.5));
            assertThat(analyticDataList.get(1).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(analyticDataList.get(1).getFigi(), Matchers.equalTo("BBG004730N88"));
        });

        verify(operationsService, times(Constants.PERIODS_TO_SEARCH_OLDER_OPERATIONS.size()))
                .getLastOperationsForStock(any(), any(), any(), any());
    }


    @DisplayName("test operations with dividend")
    @Test
    void a3testApplyPositions() {
        var positions = List.of(
            Position.builder()
                .figi("BBG004730N88")
                .ticker(SBER)
                .balance(10)
                .lots(1)
                .build()
        );

        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        map.put(NLMK, nlmkOps.stream().map(operation -> mapper.map(operation, NLMK)).toList());
        map.put(AMZN, amznOps.stream().map(operation -> mapper.map(operation, AMZN)).toList());

        var analyticDataList = processor.apply(map, positions, "3");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(NLMK));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.lessThan(200.0));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG004S681B4"));
        });
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(1).getTicker(), Matchers.equalTo(AMZN));
            assertThat(analyticDataList.get(1).getRevenue(), Matchers.equalTo(25.83));
            assertThat(analyticDataList.get(1).getDividend(), Matchers.equalTo(5.0));
            assertThat(analyticDataList.get(1).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(1).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(1).getFigi(), Matchers.equalTo("BBG000BVPV84"));
        });
    }

    @DisplayName("test add older operations")
    @Test
    void a4testOlderOps() {
        var positions = List.of(
            Position.builder()
                .figi("BBG004730N88")
                .ticker(NLMK)
                .balance(10)
                .lots(1)
                .build()
        );

        MultiValueMap<String, com.home.project.portfolio.model.operations.Operation> map = new LinkedMultiValueMap<>();
        var operations = new ArrayList<>(nlmkOps.stream().map(operation -> mapper.map(operation, NLMK)).toList());
        var toRemove = operations.get(4);
        operations.remove(4);
        map.put(NLMK, new ArrayList<>(nlmkOps.stream().map(operation -> mapper.map(operation, NLMK)).toList()));

        when(operationsService.getLastOperationsForStock(any(), any(), any(), eq("4")))
            .thenReturn(List.of(toRemove));

        var analyticDataList = processor.apply(map, positions, "4");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(NLMK));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.equalTo(-522.84));
            assertThat(analyticDataList.get(0).getDividend(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getTotalSoldSum(), Matchers.equalTo(345700.4));
            assertThat(analyticDataList.get(0).getTotalBoughtSum(), Matchers.equalTo(346223.24));
            assertThat(analyticDataList.get(0).getRevenuePercentage(), Matchers.equalTo(-0.15));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG004S681B4"));
        });
        verify(operationsService, times(1)).getLastOperationsForStock(any(), any(), any(), eq("4"));
    }
}