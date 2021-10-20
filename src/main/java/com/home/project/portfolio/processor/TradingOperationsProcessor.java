package com.home.project.portfolio.processor;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.operations.Instrument;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.response.OperationsDto;
import com.home.project.portfolio.utils.OperationGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Class to process trading operations: buy, sell, etc
 */
@Component
@Log4j2
public class TradingOperationsProcessor implements OperationsProcessor {

    private final TinkoffClient tinkoffClient;

    public TradingOperationsProcessor(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    /**
     * Add trading operations:
     *
     * @param operations - operations for the period
     * @param operationsDto - operationDto to populate
     */
    @Override
    public void apply(Operations operations, OperationsDto operationsDto) {
        var figiToTickerMap = operations.getPayload().getOperations().stream()
                .filter(operation -> OperationGroups.TRADING_OPERATIONS.contains(operation.getOperationType()))
                .map(Operation::getFigi)
                .collect(Collectors.toSet())
                .stream()
                .map(tinkoffClient::getInstrumentInfoByFigi)
                .peek(instrument -> log.debug("Retrieved instrument {}", instrument.toString()))
                .map(Instrument::getPayload)
                .collect(Collectors.toMap(Instrument.Payload::getFigi, Instrument.Payload::getTicker));

        log.debug("Converted figi to ticker for {} instruments", figiToTickerMap.size());

        operations.getPayload().getOperations().stream()
                .filter(operation -> OperationGroups.TRADING_OPERATIONS.contains(operation.getOperationType()))
                .forEach(operation -> {
                    var ticker = figiToTickerMap.get(operation.getFigi());
                    operation.setTicker(ticker);
                    log.debug("Adding operations for {}", ticker);

                    operationsDto.addOperationOnStock(ticker, operation);
                });
    }
}
