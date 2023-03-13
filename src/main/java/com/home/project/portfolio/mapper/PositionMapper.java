package com.home.project.portfolio.mapper;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.portfolio.AveragePositionItem;
import com.home.project.portfolio.model.portfolio.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.core.models.Money;

/**
 * @author rlagay
 */
@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mappings({
        @Mapping(target = "figi", source = "position.figi"),
        @Mapping(target = "instrumentType", source = "position.instrumentType", qualifiedByName = "getInstrumentType"),
        @Mapping(target = "balance", source = "position.quantity"),
        @Mapping(target = "lots", source = "position.quantityLots"),
        @Mapping(target = "averagePositionPrice", source = "position.averagePositionPrice", qualifiedByName = "getAveragePrice"),
        @Mapping(target = "expectedYield", source = "position", qualifiedByName = "getExpectedYield")
    })
    Position map(ru.tinkoff.piapi.core.models.Position position, Instrument instrument, Long blocked);

    @Named("getInstrumentType")
    default InstrumentType getStatus(String type) {
        return InstrumentType.parse(type);
    }

    @Named("getAveragePrice")
    default AveragePositionItem getAveragePrice(Money averagePositionPrice) {
        return new AveragePositionItem(Currency.parse(averagePositionPrice.getCurrency()),
            averagePositionPrice.getValue().doubleValue());
    }

    @Named("getExpectedYield")
    default AveragePositionItem getExpectedYield(ru.tinkoff.piapi.core.models.Position position) {
        return new AveragePositionItem(Currency.parse(position.getAveragePositionPrice().getCurrency()),
            position.getExpectedYield().doubleValue());
    }
}
