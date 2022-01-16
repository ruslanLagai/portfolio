package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.RevenueCalculator;
import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.service.OperationsService;
import com.home.project.portfolio.utils.Constants;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private List<Operation> alexionOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000G30YX4"))
            .collect(Collectors.toList());
    private List<Operation> biomarinOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000CZWZ05"))
            .collect(Collectors.toList());
    private List<Operation> aaplOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000B9XRY4"))
            .collect(Collectors.toList());
    private List<Operation> sberOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG0047315Y7"))
            .collect(Collectors.toList());
    private List<Operation> amznOps = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> TRADING_OPERATIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getFigi().equals("BBG000BVPV84"))
            .collect(Collectors.toList());

    private final OperationsService operationsService = mock(OperationsService.class);
    private final RevenueCalculator calculator = new RevenueCalculator();
    private final RevenueProcessor processor = new RevenueProcessor(calculator, operationsService);

    @DisplayName("test empty positions")
    @Test
    void a1apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(ALEXION, alexionOps);
        map.put("BMRN", biomarinOps);

        var analyticDataList = processor.apply(map, Collections.emptyList(), "1");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(ALEXION));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(19.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
        });
        verify(operationsService, times(0)).getLastOperationsForStock(any(), any(), any(), any());
    }

    @DisplayName("test have positions")
    @Test
    void a2applyPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, aaplOps);
        map.put(SBERP, sberOps);
        var positions = TestUtils.readPositions()
                .getPayload().getPositions().stream()
                .filter(position -> position.getTicker().equals(AAPL) || position.getTicker().equals(SBERP))
                .collect(Collectors.toList());

        var analyticDataList = processor.apply(map, positions, "2");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(1060.0));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(1060.0));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.greaterThan(1060.0));
            assertThat(analyticDataList.get(0).getRevenuePercentage(), Matchers.equalTo(15.5));
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

        verify(operationsService, times(Constants.PERIODS_TO_SEARCH_OLDER_OPERATIONS.size()))
                .getLastOperationsForStock(any(), any(), any(), any());
    }


    @DisplayName("test operations more than positions")
    @Test
    void a3testApplyPositions() {
        when(operationsService.getLastOperationsForStock(any(), any(), any(), eq("3")))
                .thenReturn(List.of(
                        Operation.builder()
                                .quantityExecuted(10)
                                .operationType(OperationType.BUY)
                                .currency(Currency.USD)
                                .status(Status.DONE)
                                .price(54.74)
                                .payment(547.4)
                                .date(ZonedDateTime.now())
                                .build(),
                        Operation.builder()
                                .quantityExecuted(6)
                                .operationType(OperationType.BUY_CARD)
                                .status(Status.DONE)
                                .currency(Currency.USD)
                                .price(54.74)
                                .payment(328.44)
                                .date(ZonedDateTime.now().minusMinutes(1))
                                .build()));

        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, aaplOps);
        map.put(AMZN, amznOps);

        var analyticDataList = processor.apply(map, AAPL_POSITIONS, "3");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.lessThan(200.0));
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
        verify(operationsService, times(1)).getLastOperationsForStock(any(), any(), any(), eq("3"));
    }

    @DisplayName("test add older operations")
    @Test
    void a4testOlderOps() {
        when(operationsService.getLastOperationsForStock(any(), any(), any(), eq("4")))
                .thenReturn(List.of(
                        Operation.builder()
                                .quantityExecuted(10)
                                .operationType(OperationType.BUY_CARD)
                                .currency(Currency.USD)
                                .status(Status.DONE)
                                .price(54.74)
                                .payment(547.4)
                                .date(ZonedDateTime.now())
                                .build(),
                        Operation.builder()
                                .quantityExecuted(6)
                                .operationType(OperationType.BUY)
                                .status(Status.DONE)
                                .currency(Currency.USD)
                                .price(54.74)
                                .payment(328.44)
                                .date(ZonedDateTime.now().minusMinutes(1))
                                .build()));

        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        var toModify = aaplOps.stream()
                        .filter(operation -> Objects.equals(operation.getId(), "77075356700L"))
                                .findFirst().orElse(new Operation());
        toModify.setOperationType(OperationType.BUY);
        map.put(AAPL, aaplOps);

        var analyticDataList = processor.apply(map, AAPL_POSITIONS, "4");
        assertAll(() -> {
            assertFalse(analyticDataList.isEmpty());
            assertThat(analyticDataList.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(analyticDataList.get(0).getRevenue(), Matchers.lessThan(200.0));
            assertThat(analyticDataList.get(0).getDividend(), Matchers.equalTo(5.41));
            assertThat(analyticDataList.get(0).getTotalBoughtSum(), Matchers.equalTo(7743.21));
            assertThat(analyticDataList.get(0).getTotalSoldSum(), Matchers.equalTo(7936.0));
            assertThat(analyticDataList.get(0).getRevenuePercentage(), Matchers.equalTo(2.4));
            assertThat(analyticDataList.get(0).getCommission(), Matchers.equalTo(0.0));
            assertThat(analyticDataList.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(analyticDataList.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        verify(operationsService, times(1)).getLastOperationsForStock(any(), any(), any(), eq("4"));
    }
}