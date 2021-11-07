package com.home.project.portfolio.service;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.entity.StockMetadata;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class to test {@link StockHelperService}
 */
@Testcontainers
@ContextConfiguration(classes = AbstractDbTest.Config.class)
@ExtendWith(SpringExtension.class)
class StockHelperServiceTest extends AbstractDbTest {

    static {
        mySQLContainer.start();
    }

    @Autowired
    StockHelperService helperService;

    @Autowired
    StockRepository stockRepository;

    @Test
    @DisplayName("test rest - stock")
    void findTicker() {
        var result = helperService.findTicker(mockOperation("BBG000B9XRY4", OperationType.BUY, Currency.USD));
        assertEquals("AAPL", result);
    }

    @Test
    @DisplayName("test rest - USD service commission")
    void findTickerTest() {
        var result = helperService.findTicker(mockOperation(null, OperationType.SERVICE_COMMISSION, Currency.USD));
        assertEquals(Constants.SERVICE_COMMISSION_USD, result);
    }

    @Test
    @DisplayName("test rest - RUB service commission")
    void findTickerTestRub() {
        var result = helperService.findTicker(mockOperation(null, OperationType.SERVICE_COMMISSION, Currency.RUB));
        assertEquals(Constants.SERVICE_COMMISSION_RUB, result);
    }

    @Test
    @DisplayName("test db - stock")
    void findTickerDb() {
        var saved = stockRepository.save(mockMetadata());

        var result = helperService.findTicker(mockOperation("BBG000B9XRY4", OperationType.BUY, Currency.USD));
        assertEquals("AAPL", result);

        stockRepository.deleteById(saved.getId());
    }

    private StockMetadata mockMetadata() {
        var stock = new StockMetadata();
        stock.setFigi("BBG000B9XRY4");
        stock.setName("Apple");
        stock.setTicker("AAPL");
        return stock;
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
}