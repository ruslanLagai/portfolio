package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.analytic.ServiceCommission;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class AnalyticUtilsTest {

    private static final String TICKER = "ticker";
    private static final String TICKER_1 = "ticker1";
    private static final String FIGI = "figi";
    private static final String FIGI_1 = "figi1";

    @Test
    @DisplayName("test empty list")
    void mergeAnalyticData() {
        var result = AnalyticUtils.mergeAnalyticData(Collections.emptyList());
        assertThat(result.getAnalyticData(), Matchers.empty());
    }

    @Test
    @DisplayName("test basic data")
    void mergeAnalyticDataTest() {

        var result = AnalyticUtils.mergeAnalyticData(mockData());

        assertAll(() -> {
            assertThat(result.getAnalyticData().size(), Matchers.equalTo(2));

            //stock 1
            assertThat(result.getAnalyticData().get(0).getRevenue(), Matchers.equalTo(100.0));
            assertThat(result.getAnalyticData().get(0).getCommission(), Matchers.equalTo(1.0));
            assertThat(result.getAnalyticData().get(0).getFigi(), Matchers.equalTo(FIGI));
            assertThat(result.getAnalyticData().get(0).getTicker(), Matchers.equalTo(TICKER));
            assertThat(result.getAnalyticData().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            //stock 2
            assertThat(result.getAnalyticData().get(1).getRevenue(), Matchers.equalTo(1000.0));
            assertThat(result.getAnalyticData().get(1).getCommission(), Matchers.equalTo(10.0));
            assertThat(result.getAnalyticData().get(1).getFigi(), Matchers.equalTo(FIGI_1));
            assertThat(result.getAnalyticData().get(1).getTicker(), Matchers.equalTo(TICKER_1));
            assertThat(result.getAnalyticData().get(1).getCurrency(), Matchers.equalTo(Currency.USD));

            //commissions
            assertThat(result.getServiceCommission().size(), Matchers.equalTo(2));
            assertThat(result.getServiceCommission().get(0).getCommission(), Matchers.equalTo(10.0));
            assertThat(result.getServiceCommission().get(1).getCommission(), Matchers.equalTo(100.0));
            assertThat(result.getServiceCommission().get(0).getCurrency(), Matchers.equalTo(Currency.USD));
            assertThat(result.getServiceCommission().get(1).getCurrency(), Matchers.equalTo(Currency.RUB));
        });
    }

    private List<AnalyticData> mockData() {
        var data1 = AnalyticData.builder()
                .ticker(TICKER)
                .currency(Currency.USD)
                .figi(FIGI)
                .isRevenue(true)
                .revenue(100.0)
                .build();
        var data2 = AnalyticData.builder()
                .ticker(TICKER)
                .currency(Currency.USD)
                .figi(FIGI)
                .isCommission(true)
                .commission(1.0)
                .build();
        var data3 = AnalyticData.builder()
                .ticker(Constants.SERVICE_COMMISSION_USD)
                .serviceCommission(ServiceCommission.builder()
                        .commission(10.0)
                        .currency(Currency.USD)
                        .build())
                .build();
        var data4 = AnalyticData.builder()
                .ticker(Constants.SERVICE_COMMISSION_RUB)
                .serviceCommission(ServiceCommission.builder()
                        .commission(100.0)
                        .currency(Currency.RUB)
                        .build())
                .build();

        var data5 = AnalyticData.builder()
                .ticker(TICKER_1)
                .currency(Currency.USD)
                .figi(FIGI_1)
                .isRevenue(true)
                .revenue(1000.0)
                .build();
        var data6 = AnalyticData.builder()
                .ticker(TICKER_1)
                .currency(Currency.USD)
                .figi(FIGI_1)
                .isCommission(true)
                .commission(10.0)
                .build();
        return Arrays.asList(data1, data2, data3, data4, data5, data6);
    }

}