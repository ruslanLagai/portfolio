package com.home.project.portfolio.processor;

import com.home.project.portfolio.calculation.TaxCalculator;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * class to test {@link TaxProcessor}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test taxes processor")
class TaxProcessorTest extends AbstractProcessorTest {

    private final TaxCalculator taxCalculator = new TaxCalculator();
    private final TaxProcessor taxProcessor = new TaxProcessor(taxCalculator);

    @Test
    @DisplayName("All operations")
    void apply() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put("taxRub", ALL_OPERATIONS);

        var result = taxProcessor.apply(map, Collections.emptyList(), "");

        assertAll(() -> {
            assertThat(result.size(), Matchers.equalTo(1));
            assertThat(result.get(0).getTaxes().size(), Matchers.equalTo(4));

            assertThat(result.get(0).getTaxes().get(0).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(2).getCurrency(), Matchers.equalTo(Currency.RUB));
            assertThat(result.get(0).getTaxes().get(3).getCurrency(), Matchers.equalTo(Currency.RUB));

            assertThat(result.get(0).getTaxes().get(0).getOperationType(), Matchers.equalTo(OperationType.TAX_BACK));
            assertThat(result.get(0).getTaxes().get(1).getOperationType(), Matchers.equalTo(OperationType.TAX));
            assertThat(result.get(0).getTaxes().get(2).getOperationType(), Matchers.equalTo(OperationType.TAX_DIVIDEND));
            assertThat(result.get(0).getTaxes().get(3).getOperationType(), Matchers.equalTo(OperationType.TAX_COUPON));

            assertThat(result.get(0).getTaxes().get(0).getTaxes(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getTaxes().get(1).getTaxes(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getTaxes().get(2).getTaxes(), Matchers.greaterThan(0.0));
            assertThat(result.get(0).getTaxes().get(3).getTaxes(), Matchers.equalTo(0.0));
        });
    }

    @Test
    @DisplayName("No taxes")
    void applyTwoStocks() {
        MultiValueMap<String, Operation> map = new LinkedMultiValueMap<>();
        map.put(AAPL, AAPL_OPS);

        var result = taxProcessor.apply(map, Collections.emptyList(), "");

        assertThat(result.size(), Matchers.equalTo(0));
    }
}