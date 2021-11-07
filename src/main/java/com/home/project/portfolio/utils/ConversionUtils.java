package com.home.project.portfolio.utils;

import com.home.project.portfolio.model.entity.OperationEntity;
import com.home.project.portfolio.model.entity.Trade;
import com.home.project.portfolio.model.operations.Commission;
import com.home.project.portfolio.model.operations.Operation;

import java.util.*;

/**
 * Utility class to perform conversions
 *  Tinkoff rest operation -> DB operation
 *  DB operation -> Tinkoff rest operation
 */
public class ConversionUtils {

    public static Collection<Operation> convertToRestOperations(Collection<OperationEntity> operationEntity) {
        List<Operation> operations = new ArrayList<>();
        operationEntity.forEach(entity -> {
            var converted = Operation.builder()
                    .id(entity.getOperationId())
                    .commission(Commission.builder()
                            .currency(entity.getCurrency())
                            .value(entity.getCommission())
                            .build())
                    .currency(entity.getCurrency())
                    .date(entity.getDate())
                    .figi(entity.getFigi())
                    .ticker(entity.getTicker())
                    .operationType(entity.getOperationType())
                    .instrumentType(entity.getInstrumentType())
                    .isMarginCall(entity.isMarginCall())
                    .payment(entity.getPayment())
                    .price(entity.getPrice())
                    .quantity(entity.getQuantity())
                    .quantityExecuted(entity.getQuantityExecuted())
                    .status(entity.getStatus())
                    .trades(convertToRestTrades(entity.getTrades()))
                    .build();
            operations.add(converted);
        });
        return operations;
    }

    public static Set<OperationEntity> convertToDbOperations(Collection<Operation> operations, String accountId) {
        Set<OperationEntity> operationEntities = new HashSet<>();
        operations.forEach(operation -> {
            var converted = OperationEntity.builder()
                    .accountId(accountId)
                    .operationId(operation.getId())
                    .figi(operation.getFigi())
                    .ticker(operation.getTicker())
                    .date(operation.getDate())
                    .currency(operation.getCurrency())
                    .payment(operation.getPayment())
                    .price(operation.getPrice())
                    .commission(operation.getCommission() != null ? operation.getCommission().getValue() : 0.0)
                    .operationType(operation.getOperationType())
                    .instrumentType(operation.getInstrumentType())
                    .isMarginCall(operation.isMarginCall())
                    .quantity(operation.getQuantity())
                    .quantityExecuted(operation.getQuantityExecuted())
                    .status(operation.getStatus())
                    .trades(convertToDbTrades(operation.getTrades(), operation))
                    .build();
            operationEntities.add(converted);
        });
        return operationEntities;
    }

    public static Set<Trade> convertToDbTrades(Collection<com.home.project.portfolio.model.operations.Trade> restTrades,
                                               Operation operation) {
        if (restTrades == null) {
             return null;
        }
        Set<Trade> trades = new HashSet<>();
        restTrades.forEach(trade -> {
            var converted = Trade.builder()
                    .tradeId(trade.getTradeId())
                    .date(trade.getDate())
                    .price(trade.getPrice())
                    .quantity(trade.getQuantity())
                    .operationId(operation.getId())
                    .build();
            trades.add(converted);
        });
        return trades;
    }

    public static List<com.home.project.portfolio.model.operations.Trade> convertToRestTrades(Collection<Trade> dbTrades) {
        if (dbTrades == null) {
            return null;
        }
        List<com.home.project.portfolio.model.operations.Trade> trades = new ArrayList<>();
        dbTrades.forEach(trade -> {
            com.home.project.portfolio.model.operations.Trade converted = new com.home.project.portfolio.model.operations.Trade();
            converted.setPrice(trade.getPrice());
            converted.setQuantity(trade.getQuantity());
            converted.setDate(trade.getDate());
            converted.setTradeId(trade.getTradeId());
            trades.add(converted);
        });
        return trades;
    }
}
