package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.operations.StockAvailability;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.org.apache.commons.lang.time.DateUtils;

import java.time.ZonedDateTime;

import static com.home.project.portfolio.utils.Profiles.TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link PortfolioService}
 */
@DisplayName("Test Tinkoff service")
@ExtendWith(SpringExtension.class)
@ActiveProfiles(TEST_PROFILE)
@ContextConfiguration(classes = {PortfolioServiceTest.Config.class})
@Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
class PortfolioServiceTest {

    @Autowired
    PortfolioService portfolioService;

    @BeforeEach
    public void before() throws InterruptedException {
        Thread.sleep(60000);
    }

    @Test
    @DisplayName("test get portfolio")
    void getPortfolio() {
        var result = portfolioService.getPortfolio();
        assertAll(() -> {
            assertEquals(2, result.getPositions().size());
            result.getPositions().forEach((k, v) -> {
                assertThat(v.size(), Matchers.greaterThan(5));
                assertTrue(StringUtils.isNotBlank(v.get(0).getTicker()));
                assertTrue(StringUtils.isNotBlank(v.get(2).getName()));
                assertThat(v.get(4).getBalance(), Matchers.greaterThan(0.0));
            });
            result.getPrices().forEach((k, v) -> {
                assertThat(v.size(), Matchers.lessThan(2));
                assertThat(v.get(0).getLastPrice(), Matchers.greaterThan(0.0));
                v.stream().filter(overbook -> overbook.getTradeStatus().equals(StockAvailability.AVAILABLE))
                        .forEach(overbook -> {
                            assertThat(overbook.getDepth(), Matchers.equalTo(1));
                            assertThat(overbook.getLimitDown(), Matchers.greaterThan(0.0));
                            assertThat(overbook.getLimitUp(), Matchers.greaterThan(0.0));
                            assertThat(overbook.getAsks().get(0).getPrice(), Matchers.greaterThan(0.0));
                            assertThat(overbook.getBids().get(0).getPrice(), Matchers.greaterThan(0.0));
                        });
                v.stream().filter(overbook -> overbook.getTradeStatus().equals(StockAvailability.NOT_AVAILABLE))
                        .forEach(overbook -> {
                            assertThat(overbook.getLimitDown(), Matchers.equalTo(0.0));
                            assertThat(overbook.getLimitUp(), Matchers.equalTo(0.0));
                            assertThat(overbook.getAsks().size(), Matchers.equalTo(0));
                            assertThat(overbook.getBids().size(), Matchers.equalTo(0));
                        });
            });
        });
    }

    @Test
    @DisplayName("test get operations")
    void testGetPortfolio() {
        var result = portfolioService.getLastOperations("2000686010", Period.LAST_MONTH);
        assertAll(() -> {
            assertFalse(result.getOperationsByTicker().isEmpty());
            result.getOperationsByTicker().keySet().forEach(k -> {
                assertFalse(result.getOperationsByTicker().get(k).isEmpty());
                assertFalse(StringUtils.isBlank(result.getOperationsByTicker().get(k).get(0).getTicker()));
                assertThat(result.getOperationsByTicker().get(k).get(0).getDate(),
                        Matchers.greaterThan(ZonedDateTime.now().minus(java.time.Period.ofDays(31))));
            });
        });
    }

    @SneakyThrows
    @DisplayName("parametrized test get operations")
    @ParameterizedTest
    @EnumSource(value = Period.class, names = {"LAST_MONTH", "LAST_DAY"}, mode = EnumSource.Mode.EXCLUDE)
    void testGetOperationParam(Period period) {
        Thread.sleep(DateUtils.MILLIS_PER_MINUTE);
        var date = ZonedDateTime.now().minusDays(1);
        var result = portfolioService.getLastOperations("2000686010", period);
        assertAll(() -> {
            assertFalse(result.getOperationsByTicker().isEmpty());
            result.getOperationsByTicker().keySet().forEach(k -> {
                assertFalse(result.getOperationsByTicker().get(k).isEmpty());
                assertFalse(StringUtils.isBlank(result.getOperationsByTicker().get(k).get(0).getTicker()));
                assertThat(result.getOperationsByTicker().get(k).get(0).getDate(),
                        Matchers.greaterThan(date.minus(period.getPeriodDuration())));
            });
        });
    }

    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.portfolio.processor", "com.home.project.portfolio.service",
            "com.home.project.portfolio.calculation"})
    @EnableFeignClients(clients = TinkoffClient.class)
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {
    }
}