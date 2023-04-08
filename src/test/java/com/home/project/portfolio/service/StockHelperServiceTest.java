package com.home.project.portfolio.service;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.entity.StockMetadata;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.core.InstrumentsService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Class to test {@link StockHelperService}
 */
@DisplayName("Test helper service")
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = AbstractDbTest.Initializer.class)
class StockHelperServiceTest extends AbstractDbTest {

    @Autowired
    private StockHelperService helperService;

    @Autowired
    private StockRepository stockRepository;

    @MockBean
    private InstrumentsService instrumentsService;

    @Test
    @DisplayName("test rest - stock")
    void findTicker() {
        when(instrumentsService.getInstrumentByFigiSync("BBG004S681B4")).thenReturn(Instrument.newBuilder()
            .setFigi("BBG004S681B4")
            .setTicker("NLMK")
            .setIsin("RU0009046452")
            .setName("НЛМК")
            .setInstrumentType("share")
            .setTradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_DEALER_NORMAL_TRADING)
            .build());
        var result = helperService.findTicker(mockOperation("BBG004S681B4", OperationType.OPERATION_TYPE_BUY, Currency.RUB));
        var saved = stockRepository.getByFigi("BBG004S681B4");
        assertEquals("NLMK", result);
        assertAll(() -> {
            assertEquals("NLMK", saved.getTicker());
            assertEquals("BBG004S681B4", saved.getFigi());
            assertEquals("RU0009046452", saved.getIsin());
            assertEquals("НЛМК", saved.getName());
            assertEquals(InstrumentType.STOCK, saved.getInstrumentType());

        });
    }

    @Test
    @DisplayName("test - USD service commission")
    void findTickerTest() {
        var result = helperService.findTicker(mockOperation("", OperationType.OPERATION_TYPE_BROKER_FEE, Currency.USD));
        assertEquals(Constants.SERVICE_COMMISSION_USD, result);
    }

    @Test
    @DisplayName("test - RUB service commission")
    void findTickerTestRub() {
        var result = helperService.findTicker(mockOperation("", OperationType.OPERATION_TYPE_SERVICE_FEE, Currency.RUB));
        assertEquals(Constants.SERVICE_COMMISSION_RUB, result);
    }

    @Test
    @DisplayName("test db - stock")
    void findTickerDb() {
        var saved = stockRepository.save(mockMetadata());

        var result = helperService.findTicker(mockOperation("BBG000B9XRY4", OperationType.OPERATION_TYPE_BUY, Currency.USD));
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
        var operation = Operation.newBuilder();
        operation.setOperationType(operationType);
        operation.setPayment(MoneyValue.newBuilder().setUnits(-100).setNano(0).build());
        operation.setQuantity(1);
        operation.setCurrency(currency.getCode());
        operation.setFigi(figi);
        return operation.build();
    }
}