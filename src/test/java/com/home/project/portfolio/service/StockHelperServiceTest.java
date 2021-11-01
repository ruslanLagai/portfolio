package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.helpers.YamlPropertySourceFactory;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.utils.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class to test {@link StockHelperService}
 */
@ContextConfiguration(classes = StockHelperServiceTest.Config.class)
@Import({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
@ExtendWith(SpringExtension.class)
class StockHelperServiceTest {

    @Autowired
    StockHelperService helperService;

    @Test
    @DisplayName("test stock")
    void findTicker() {
        var result = helperService.getTickerFromTinkoff(mockOperation("BBG000B9XRY4", OperationType.BUY, Currency.USD));
        assertEquals("AAPL", result);
    }

    @Test
    @DisplayName("test USD service commission")
    void findTickerTest() {
        var result = helperService.getTickerFromTinkoff(mockOperation(null, OperationType.SERVICE_COMMISSION, Currency.USD));
        assertEquals(Constants.SERVICE_COMMISSION_USD, result);
    }

    @Test
    @DisplayName("test RUB service commission")
    void findTickerTestRub() {
        var result = helperService.findTicker(mockOperation(null, OperationType.SERVICE_COMMISSION, Currency.RUB));
        assertEquals(Constants.SERVICE_COMMISSION_RUB, result);
    }

    private Operation mockOperation(String figi, OperationType operationType, Currency currency) {
        var operation = new Operation();
        operation.setOperationType(operationType);
        operation.setPayment((-100.0));
        operation.setQuantity(1);
        operation.setCurrency(currency);
        operation.setFigi(figi);
        return operation;
    }

    @Configuration
    @EnableFeignClients(clients = TinkoffClient.class)
    @PropertySource(value = "classpath:application-test.yml", factory = YamlPropertySourceFactory.class)
    static class Config {
        @Bean
        public StockHelperService stockHelperService(TinkoffClient tinkoffClient) {
            return new StockHelperService(tinkoffClient);
        }
    }
}