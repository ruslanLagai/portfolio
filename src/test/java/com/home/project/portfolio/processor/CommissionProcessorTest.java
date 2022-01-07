package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.CommissionCalculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.utils.OperationGroups;
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

import static com.home.project.portfolio.utils.Constants.SERVICE_COMMISSION_RUB;
import static com.home.project.portfolio.utils.Constants.SERVICE_COMMISSION_USD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Class to test {@link CommissionCalculator}
 */
@ExtendWith(MockitoExtension.class)
class CommissionProcessorTest extends AbstractProcessorTest {

    private static final List<Operation> COMMISSIONS_RUB = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> operation.getFigi() == null)
            .filter(operation -> OperationGroups.COMMISSIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getCurrency().equals(Currency.RUB))
            .collect(Collectors.toList());
    private static final List<Operation> COMMISSIONS_USD = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> operation.getFigi() == null)
            .filter(operation -> OperationGroups.COMMISSIONS.contains(operation.getOperationType()))
            .filter(operation -> operation.getCurrency().equals(Currency.USD))
            .collect(Collectors.toList());
    private static final List<Operation> AAPL_OPS = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> operation.getFigi() != null && operation.getFigi().equals("BBG000B9XRY4"))
            .collect(Collectors.toList());
    private static final List<Operation> AMZN_OPS = OPERATIONS
            .getPayload().getOperations().stream()
            .filter(operation -> operation.getStatus().equals(Status.DONE))
            .filter(operation -> operation.getFigi() != null && operation.getFigi().equals("BBG000BVPV84"))
            .collect(Collectors.toList());

    CommissionCalculator calculator = new CommissionCalculator();
    CommissionProcessor processor = new CommissionProcessor(calculator);

    @Test
    @DisplayName("Calculate commission - AAPL")
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, AAPL_OPS);
        map.put(SERVICE_COMMISSION_RUB, COMMISSIONS_RUB);
        map.put(SERVICE_COMMISSION_USD, COMMISSIONS_USD);

        var result = processor.apply(map, AAPL_POSITIONS, "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(3));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(result.get(0).getCommission(), Matchers.greaterThan(7.0));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
        });
        assertAll(() -> {
            assertThat(result.get(1).getServiceCommission().getCommission(), Matchers.greaterThan(3000.0));
            assertThat(result.get(1).getServiceCommission().getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(2).getServiceCommission().getCommission(), Matchers.greaterThan(30.0));
            assertThat(result.get(2).getServiceCommission().getCurrency(), Matchers.equalTo(Currency.USD));
        });
    }

    @Test
    @DisplayName("Calculate commission - AAPL, AMZN")
    void applyTwoStocks() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, AAPL_OPS);
        map.put(AMZN, AMZN_OPS);

        var result = processor.apply(map, AAPL_AMZN_POSITIONS, "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(2));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCommission(), Matchers.greaterThan(7.0));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
        });

        assertAll(() -> {
            assertThat(result.get(1).getTicker(), Matchers.equalTo(AMZN));
            assertThat(result.get(1).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(1).getCommission(), Matchers.greaterThan(130.0));
            assertThat(result.get(1).getFigi(), Matchers.equalTo("BBG000BVPV84"));
            assertThat(result.get(1).getCurrency(), Matchers.equalTo(Currency.USD));
        });
    }

    @Test
    @DisplayName("Calculate commission - no positions")
    void applyNoPositions() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, AAPL_OPS);

        var result = processor.apply(map, Collections.emptyList(), "");
        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getTicker(), Matchers.equalTo(AAPL));
            assertThat(result.get(0).getRevenue(), Matchers.equalTo(0.0));
            assertThat(result.get(0).getCommission(), Matchers.greaterThan(7.0));
            assertThat(result.get(0).getFigi(), Matchers.equalTo("BBG000B9XRY4"));
            assertThat(result.get(0).getCurrency(), Matchers.equalTo(Currency.USD));
        });
    }

}