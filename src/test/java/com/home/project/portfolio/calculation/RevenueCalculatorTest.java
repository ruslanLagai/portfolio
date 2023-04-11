package com.home.project.portfolio.calculation;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.mapper.OperationMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Class to test {@link RevenueCalculator}
 */
@ExtendWith(MockitoExtension.class)
class RevenueCalculatorTest {

    RevenueCalculator calculator = new RevenueCalculator();
    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    public static final String ACCOUNT_ID = "2000686010";

    @Test
    @DisplayName("Test lot of operations: AMZN")
    void calculateRevenue() {
        var operations = TestUtils.operations("classpath:testData/get-operations.json");
        var amznOperations = operations.stream()
            .filter(operation -> operation.getFigi().equals("BBG000BVPV84"))
            .map(operation -> operationMapper.map(operation, ACCOUNT_ID))
            .toList();
        var result = calculator.calculate(amznOperations);
        assertThat(result, Matchers.equalTo(25.83));
    }

    @Test
    @DisplayName("Test operations: MGNT")
    void calculateRevenueTest() {
        var operations = TestUtils.operations("classpath:testData/get-operations.json");
        var mgntOperations = operations.stream()
            .filter(operation -> operation.getFigi().equals("BBG004RVFCY3"))
            .map(operation -> operationMapper.map(operation, ACCOUNT_ID))
            .toList();
        var result = calculator.calculate(mgntOperations);
        assertThat(result, Matchers.equalTo(70187.74));
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