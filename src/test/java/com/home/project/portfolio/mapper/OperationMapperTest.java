package com.home.project.portfolio.mapper;

import com.home.project.portfolio.helpers.TestUtils;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.operations.Commission;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.operations.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_ACCRUING_VARMARGIN;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_BROKER_FEE;
import static ru.tinkoff.piapi.contract.v1.OperationType.OPERATION_TYPE_SELL;

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OperationMapperTest {

    OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Order(1)
    @ParameterizedTest
    @MethodSource("getArgs")
    @DisplayName("test operation mapping")
    void mapAccount(String path, String ticker, Operation expected) {
        var result = mapper.map(TestUtils.operation(path), ticker);
        assertEquals(expected, result);
    }

    @Order(2)
    @Test
    @DisplayName("test mapping to entity")
    void mapToEntities() {
        var result = mapper.mapToEntities(List.of(tradeOperation(), marginOperation(), feeOperation()), "123");

        var trade = result.get(0);
        assertAll(() -> {
            assertEquals("2022426539781660682", trade.getOperationId());
            assertEquals("FUTCNY032300", trade.getFigi());
            assertEquals(ZonedDateTime.parse("2023-01-11T08:15:05.215Z[UTC]"), trade.getDate());
            assertEquals("ticker", trade.getTicker());
            assertEquals(10.292, trade.getPrice());
            assertEquals(30876, trade.getPayment());
            assertEquals("123", trade.getAccountId());
            assertEquals(0.0, trade.getCommission());
            assertEquals(Currency.RUB, trade.getCurrency());
            assertEquals(InstrumentType.FUTURES, trade.getInstrumentType());
            assertEquals(OPERATION_TYPE_SELL, trade.getOperationType());
            assertEquals(3, trade.getQuantityExecuted());
            assertEquals(Status.DONE, trade.getStatus());
            assertEquals(3, trade.getQuantity());
            assertEquals(1, trade.getTrades().size());
            assertEquals("2022426539781542581", trade.getTrades().iterator().next().getTradeId());
            assertEquals("2022426539781660682", trade.getTrades().iterator().next().getOperationId());
            assertEquals(10.292, trade.getTrades().iterator().next().getPrice());
            assertEquals(3, trade.getTrades().iterator().next().getQuantity());
            assertEquals(ZonedDateTime.parse("2023-01-11T08:15:05.215Z[UTC]"), trade.getTrades().iterator().next().getDate());
        });

        var margin = result.get(1);
        assertAll(() -> {
            assertEquals("398070874", margin.getOperationId());
            assertEquals("", margin.getFigi());
            assertEquals(ZonedDateTime.parse("2023-01-11T16:09:32.189Z[UTC]"), margin.getDate());
            assertNull(margin.getTicker());
            assertEquals(0.0, margin.getPrice());
            assertEquals("123", margin.getAccountId());
            assertEquals(0.0, margin.getCommission());
            assertEquals(Currency.RUB, margin.getCurrency());
            assertNull(margin.getInstrumentType());
            assertEquals(0, margin.getQuantityExecuted());
            assertEquals(Status.DONE, margin.getStatus());
            assertEquals(0, margin.getQuantity());
            assertTrue(margin.getTrades().isEmpty());
            assertEquals(OPERATION_TYPE_ACCRUING_VARMARGIN, margin.getOperationType());
        });

        var fee = result.get(2);
        assertAll(() -> {
            assertEquals("3673707041", fee.getOperationId());
            assertEquals("FUTCNY032300", fee.getFigi());
            assertEquals(ZonedDateTime.parse("2023-01-11T08:15:06.215Z[UTC]"), fee.getDate());
            assertEquals("ticker", fee.getTicker());
            assertEquals(0.0, fee.getPrice());
            assertEquals(-7.72, fee.getPayment());
            assertEquals("123", fee.getAccountId());
            assertEquals(0.0, fee.getCommission());
            assertEquals(Currency.RUB, fee.getCurrency());
            assertEquals(0, fee.getQuantityExecuted());
            assertEquals(Status.DONE, fee.getStatus());
            assertEquals(0, fee.getQuantity());
            assertTrue(fee.getTrades().isEmpty());
            assertEquals(OPERATION_TYPE_BROKER_FEE, fee.getOperationType());
            assertEquals(InstrumentType.FUTURES, fee.getInstrumentType());
        });
    }

    @Order(3)
    @Test
    @DisplayName(("test mapping from entity"))
    void mapToRest() {
        var entities = mapper.mapToEntities(List.of(tradeOperation(), marginOperation(), feeOperation()), "123");

        var result = mapper.mapToRest(entities);

        var expectedTrade = tradeOperation();
        expectedTrade.setCommission(Commission.builder().currency(Currency.RUB).build());
        assertEquals(expectedTrade, result.get(0));

        var expectedMargin = marginOperation();
        expectedMargin.setCommission(Commission.builder().currency(Currency.RUB).build());
        assertEquals(expectedMargin, result.get(1));

        var expectedFee = feeOperation();
        expectedFee.setCommission(Commission.builder().currency(Currency.RUB).build());
        assertEquals(expectedFee, result.get(2));

    }

    private static Stream<Arguments> getArgs() {
        return Stream.of(
            Arguments.of("classpath:testData/mapper/trade-operation.json", "ticker", tradeOperation()),
            Arguments.of("classpath:testData/mapper/margin-operation.json", null, marginOperation()),
            Arguments.of("classpath:testData/mapper/fee-operation.json", "ticker", feeOperation())
        );
    }

    private static Operation tradeOperation() {
        return Operation.builder()
            .payment(30876)
            .quantity(3)
            .quantityExecuted(3)
            .trades(List.of(
                new Trade("2022426539781542581", ZonedDateTime.parse("2023-01-11T08:15:05.215Z[UTC]"), 3, 10.292)
            ))
            .isMarginCall(false)
            .instrumentType(InstrumentType.FUTURES)
            .figi("FUTCNY032300")
            .operationType(OPERATION_TYPE_SELL)
            .ticker("ticker")
            .currency(Currency.RUB)
            .price(10.292)
            .status(Status.DONE)
            .date(ZonedDateTime.parse("2023-01-11T08:15:05.215Z[UTC]"))
            .id("2022426539781660682")
            .build();
    }

    private static Operation marginOperation() {
        return Operation.builder()
            .payment(162)
            .quantity(0)
            .quantityExecuted(0)
            .trades(List.of())
            .isMarginCall(false)
            .figi("")
            .operationType(OPERATION_TYPE_ACCRUING_VARMARGIN)
            .currency(Currency.RUB)
            .price(0.0)
            .status(Status.DONE)
            .date(ZonedDateTime.parse("2023-01-11T16:09:32.189Z[UTC]"))
            .id("398070874")
            .build();
    }

    private static Operation feeOperation() {
        return Operation.builder()
            .payment(-7.72)
            .quantity(0)
            .quantityExecuted(0)
            .trades(List.of())
            .isMarginCall(false)
            .instrumentType(InstrumentType.FUTURES)
            .figi("FUTCNY032300")
            .ticker("ticker")
            .operationType(OPERATION_TYPE_BROKER_FEE)
            .currency(Currency.RUB)
            .price(0.0)
            .status(Status.DONE)
            .date(ZonedDateTime.parse("2023-01-11T08:15:06.215Z[UTC]"))
            .id("3673707041")
            .build();
    }


}