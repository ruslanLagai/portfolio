package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.model.operations.StockAvailability;
import com.home.project.portfolio.model.portfolio.Account;
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
        var accountId = portfolioService.getAccounts().stream()
                .filter(account -> account.getBrokerAccountType().equals(Account.AccountType.Tinkoff))
                .map(Account::getBrokerAccountId)
                .findFirst()
                .orElse(null);
        assertThat(accountId, Matchers.notNullValue());

        var result = portfolioService.getPortfolio(accountId);
        assertAll(() -> {
            assertThat(result.getPositions().size(), Matchers.greaterThan(3));
            result.getPositions().forEach(position -> {
                assertTrue(StringUtils.isNotBlank(position.getTicker()));
                assertTrue(StringUtils.isNotBlank(position.getName()));
                assertThat(position.getBalance(), Matchers.greaterThan(0.0));
            });
        });

        //prices
        assertAll(() ->
            result.getPrices().forEach((k, v) -> {
            assertThat(v.getLastPrice(), Matchers.greaterThan(0.0));
            if (v.getTradeStatus().equals(StockAvailability.AVAILABLE)) {
                assertThat(v.getDepth(), Matchers.equalTo(1));
                assertThat(v.getLimitDown(), Matchers.greaterThan(0.0));
                assertThat(v.getLimitUp(), Matchers.greaterThan(0.0));
                assertThat(v.getAsks().get(0).getPrice(), Matchers.greaterThan(0.0));
                assertThat(v.getBids().get(0).getPrice(), Matchers.greaterThan(0.0));
            }
            if (v.getTradeStatus().equals(StockAvailability.NOT_AVAILABLE)) {
                assertThat(v.getAsks().size(), Matchers.equalTo(0));
                assertThat(v.getBids().size(), Matchers.equalTo(0));
            }
        }));

        // cash
        assertAll(() ->
                result.getCash().forEach((currency, value) -> {
                    assertFalse(result.getCash().isEmpty());
                    assertThat(value, Matchers.greaterThanOrEqualTo(0.0));
                }));

        // distribution
        assertAll(() -> {
            assertThat(result.getDistribution().getAssetsInRub(), Matchers.greaterThan(0.0));
            assertThat(result.getDistribution().getAssetsInUsd(), Matchers.lessThan(result.getDistribution().getAssetsInRub()));
            assertThat(result.getDistribution().getTotalInStocks(), Matchers.greaterThan(0.0));

            assertThat(result.getDistribution().getTotalInFunds(), Matchers.greaterThan(0.0));
            assertThat(result.getDistribution().getTotalInBounds(), Matchers.equalTo(0.0));
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