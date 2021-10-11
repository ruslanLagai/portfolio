package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.portfolio.Account;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

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
                        Matchers.greaterThan(LocalDateTime.now().minus(java.time.Period.ofDays(31))));
            });
        });
    }

    @TestConfiguration
    @EnableFeignClients(clients = TinkoffClient.class)
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {

        @Bean
        PortfolioService alphaVantageService(TinkoffClient tinkoffClient) {
            return new PortfolioService(tinkoffClient);
        }
    }
}