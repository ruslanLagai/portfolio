package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.model.operations.StockAvailability;
import com.home.project.portfolio.processor.AccountProcessor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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
                            assertThat(overbook.getAsks().size(), Matchers.equalTo(0));
                            assertThat(overbook.getBids().size(), Matchers.equalTo(0));
                        });
            });
        });
    }

    @Profile(TEST_PROFILE)
    @TestConfiguration
    @ComponentScan(basePackages = {"com.home.project.portfolio.processor",
            "com.home.project.portfolio.calculation"},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                    value = {AbstractDbTest.Config.class})}
    )
    @EnableFeignClients(clients = TinkoffClient.class)
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {

        @Bean
        public PortfolioService portfolioService(TinkoffClient tinkoffClient,
                                                 List<AccountProcessor> accountProcessors) {
            return new PortfolioService(tinkoffClient, accountProcessors);
        }

        @MockBean
        OperationsService operationsService;
    }
}