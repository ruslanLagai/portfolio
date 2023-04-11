package com.home.project.portfolio.mapper;

import com.google.protobuf.Timestamp;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.entity.OperationEntity;
import com.home.project.portfolio.model.operations.Commission;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Status;
import com.home.project.portfolio.model.operations.Trade;
import com.home.project.portfolio.utils.DateUtil;
import com.home.project.portfolio.utils.PriceUtils;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
@Mapper(componentModel = "spring")
public interface OperationMapper {

    @Mappings({
        @Mapping(target = "payment", source = "operation.payment", qualifiedByName = "getPayment"),
        @Mapping(target = "date", source = "operation.date", qualifiedByName = "getDate"),
        @Mapping(target = "price", source = "operation.price", qualifiedByName = "getPrice"),
        @Mapping(target = "currency", source = "operation.currency", qualifiedByName = "getCurrency"),
        @Mapping(target = "instrumentType", source = "operation.instrumentType", qualifiedByName = "getInstrumentType"),
        @Mapping(target = "quantityExecuted", source = "operation", qualifiedByName = "getQuantityExecuted"),
        @Mapping(target = "status", source = "operation.state", qualifiedByName = "getState"),
        @Mapping(target = "trades", source = "operation", qualifiedByName = "getTrades")
    })
    Operation map(ru.tinkoff.piapi.contract.v1.Operation operation, String ticker);

    @IterableMapping(qualifiedByName = "toEntity")
    ArrayList<OperationEntity> mapToEntities(Collection<Operation> operations, @Context String accountId);

    @Named("toEntity")
    @Mappings({
        @Mapping(target = "operationId", source = "operation.id"),
        @Mapping(target = "trades", source = "operation", qualifiedByName = "toTrades"),
        @Mapping(target = "commission", source = "operation.commission.value"),
        @Mapping(target = "accountId", source = "operation", qualifiedByName = "getAccountId"),
        @Mapping(target = "id", ignore = true)
    })
    OperationEntity toEntity(Operation operation, @Context String accountId);

    @IterableMapping(qualifiedByName = "toRestOperation")
    ArrayList<Operation> mapToRest(List<OperationEntity> entities);

    @Named("toRestOperation")
    @Mappings({
        @Mapping(target = "id", source = "operationEntity.operationId"),
        @Mapping(target = "trades", source = "operationEntity.trades", qualifiedByName = "toOperationTrades"),
        @Mapping(target = "commission", source = "operationEntity", qualifiedByName = "toCommission"),
    })
    Operation toOperation(OperationEntity operationEntity);


    @Named("toCommission")
    default Commission toCommission(OperationEntity operation) {
        return Commission.builder().currency(operation.getCurrency()).value(operation.getCommission()).build();
    }

    @Named("getAccountId")
    default String getAccountId(Operation operation, @Context String accountId) {
        return accountId;
    }

    @Named("toOperationTrades")
    default List<Trade> toOperationTrades(Set<com.home.project.portfolio.model.entity.Trade> trades) {
        return trades.stream()
            .map(trade -> new Trade(trade.getTradeId(), trade.getDate(), trade.getQuantity(), trade.getPrice()))
            .collect(Collectors.toList());
    }

    @Named("toTrades")
    default Set<com.home.project.portfolio.model.entity.Trade> toTrades(Operation operation) {
        return operation.getTrades().stream()
            .map(operationTrade ->
                com.home.project.portfolio.model.entity.Trade.builder()
                    .quantity(operationTrade.getQuantity())
                    .date(operationTrade.getDate())
                    .price(operationTrade.getPrice())
                    .tradeId(operationTrade.getTradeId())
                    .operationId(operation.getId())
                    .build()
                )
            .collect(Collectors.toSet());
    }

    @Named("getTrades")
    default List<Trade> getTrades(ru.tinkoff.piapi.contract.v1.Operation operation) {
        return operation.getTradesList().stream()
            .map(operationTrade ->
                new Trade(operationTrade.getTradeId(), DateUtil.toDateTime(operationTrade.getDateTime()),
                    (int) operationTrade.getQuantity(), PriceUtils.toDoubleValue(operationTrade.getPrice()))
            )
            .collect(Collectors.toList());
    }

    @Named("getState")
    default Status getState(OperationState state) {
        return Status.parse(state);
    }

    @Named("getQuantityExecuted")
    default double getQuantityExecuted(ru.tinkoff.piapi.contract.v1.Operation operation) {
        return operation.getQuantity() - operation.getQuantityRest();
    }

    @Named("getPayment")
    default double getPayment(MoneyValue moneyValue) {
        return PriceUtils.toDoubleValue(moneyValue);
    }

    @Named("getPrice")
    default double getPrice(MoneyValue moneyValue) {
        return PriceUtils.toDoubleValue(moneyValue);
    }

    @Named("getDate")
    default ZonedDateTime getDate(Timestamp timestamp) {
        var instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return instant.atZone(ZoneId.of("UTC"));
    }

    @Named("getCurrency")
    default Currency getCurrency(String code) {
        return Currency.parse(code);
    }

    @Named("getInstrumentType")
    default InstrumentType getInstrumentType(String code) {
        return StringUtils.hasText(code) ? InstrumentType.parse(code) : null;
    }

}
