package com.home.project.portfolio.mapper;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension.class)
class PositionMapperTest {

    private static final String FIGI = "figi";
    private static final String TICKER = "ticker";
    private static final String ISIN = "isin";
    private static final String NAME = "name";

    PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @ParameterizedTest
    @ValueSource(strings = {"currency", "share", "future", "bond", "etf"})
    @DisplayName("test mapping")
    void map(String instrumentType) {
        var result = mapper.map(position(instrumentType), instrument(), 10L);
        assertAll(() -> {
            assertEquals(FIGI, result.getFigi());
            assertEquals(TICKER, result.getTicker());
            assertEquals(ISIN, result.getIsin());
            assertEquals(NAME, result.getName());
            assertEquals(10.0, result.getBlocked());
            assertEquals(10.0, result.getBalance());
            assertEquals(1, result.getLots());
            assertEquals(Currency.RUB, result.getAveragePositionPrice().currency());
            assertEquals(10.0, result.getAveragePositionPrice().value());
            assertEquals(Currency.RUB, result.getExpectedYield().currency());
            assertEquals(100.0, result.getExpectedYield().value());
            assertEquals(InstrumentType.parse(instrumentType), result.getInstrumentType());
        });
    }

    private Instrument instrument() {
        return Instrument.newBuilder()
            .setFigi(FIGI)
            .setTicker(TICKER)
            .setIsin(ISIN)
            .setName(NAME)
            .build();
    }

    private Position position(String instrumentType) {
        return Position.builder()
            .figi(FIGI)
            .averagePositionPrice(Money.builder()
                .currency("rub")
                .value(BigDecimal.valueOf(10.0))
                .build())
            .expectedYield(BigDecimal.valueOf(100.0))
            .quantity(BigDecimal.valueOf(10))
            .quantityLots(BigDecimal.valueOf(1))
            .instrumentType(instrumentType)
            .build();
    }

}