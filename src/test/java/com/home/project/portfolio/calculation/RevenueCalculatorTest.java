package com.home.project.portfolio.calculation;

import com.home.project.portfolio.helpers.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Class to test {@link RevenueCalculator}
 */
@Disabled("Temporarily disabled due to refactoring")
@ExtendWith(MockitoExtension.class)
class RevenueCalculatorTest {

    RevenueCalculator calculator = new RevenueCalculator();

    @Test
    @DisplayName("Test lot of operations: TSLA")
    void calculateRevenue() {
        var operations = TestUtils.readOperations();
        var teslaOperations = operations.getPayload().getOperations().stream()
                .filter(operation -> operation.getFigi() != null)
                .filter(operation -> operation.getFigi().equals("BBG000N9MNX3"))
                .collect(Collectors.toList());
        var result = calculator.calculate(teslaOperations);
        assertThat(result, Matchers.greaterThan(10.0));
    }

    @Test
    @DisplayName("Test operations: MGNT")
    void calculateRevenueTest() {
        var operations = TestUtils.readOperations();
        var teslaOperations = operations.getPayload().getOperations().stream()
                .filter(operation -> operation.getFigi() != null)
                .filter(operation -> operation.getFigi().equals("BBG004RVFCY3"))
                .collect(Collectors.toList());
        var result = calculator.calculate(teslaOperations);
        assertThat(result, Matchers.greaterThan(0.0));
    }

    @Test
    @DisplayName("Test empty list")
    void calculateRevenueEmptyList() {
        var result = calculator.calculate(Collections.emptyList());
        assertThat(result, Matchers.equalTo(0.0));
    }

    @Test
    @DisplayName("Test null param")
    void calculateRevenuenull() {
        var result = calculator.calculate(null);
        assertThat(result, Matchers.equalTo(0.0));
    }

}