package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.entity.StockMetadata;
import com.home.project.portfolio.model.operations.Instrument;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.OperationType;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.Constants;
import com.home.project.portfolio.utils.ExecutorServiceUtils;
import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Service to help
 */
@Service
@Log4j2
public class StockHelperService {

    private static final Function<Operation, String> PAYMENT_IN_PROC = operation ->
            operation.getCurrency().equals(Currency.USD) ? Constants.PAY_IN_USD
                    : operation.getCurrency().equals(Currency.RUB) ? Constants.PAY_IN_RUB
                    : operation.getCurrency().equals(Currency.EUR) ? Constants.PAY_IN_EUR : null;
    private static final Function<Operation, String> PAYMENT_OUT_PROC = operation ->
            operation.getCurrency().equals(Currency.USD) ? Constants.PAY_OUT_USD
                    : operation.getCurrency().equals(Currency.RUB) ? Constants.PAY_OUT_RUB
                    : operation.getCurrency().equals(Currency.EUR) ? Constants.PAY_OUT_EUR : null;
    private static final Function<Operation, String> TAX_PROC = operation -> Constants.TAX_RUB;
    private static final Function<Operation, String> SERVICE_COMMISSION_PROC = operation ->
            operation.getCurrency().equals(Currency.RUB) ? Constants.SERVICE_COMMISSION_RUB
                    : operation.getCurrency().equals(Currency.USD) ? Constants.SERVICE_COMMISSION_USD
                    : null;

    private static final Map<OperationType, Function<Operation, String>> SPECIAL_TICKERS = Map.of(
            OperationType.PAY_IN, PAYMENT_IN_PROC,
            OperationType.PAY_OUT, PAYMENT_OUT_PROC,
            OperationType.TAX, TAX_PROC,
            OperationType.TAX_BACK, TAX_PROC,
            OperationType.TAX_DIVIDEND, TAX_PROC,
            OperationType.TAX_COUPON, TAX_PROC,
            OperationType.SERVICE_COMMISSION, SERVICE_COMMISSION_PROC,
            OperationType.MARGIN_COMMISSION, SERVICE_COMMISSION_PROC,
            OperationType.OTHER_COMMISSION, SERVICE_COMMISSION_PROC,
            OperationType.EXCHANGE_COMMISSION, SERVICE_COMMISSION_PROC
    );

    private final TinkoffClient tinkoffClient;
    private final StockRepository stockRepository;

    public StockHelperService(TinkoffClient tinkoffClient,
                              StockRepository stockRepository) {
        this.tinkoffClient = tinkoffClient;
        this.stockRepository = stockRepository;
    }

    public String findTicker(Operation operation) {
        return getTickerFromDb(operation)
                .orElseGet(() -> getTickerFromTinkoff(operation));
    }

    public List<Position> getPortfolio(String accountId) {

        return null;
    }

    private Optional<String> getTickerFromDb(Operation operation) {
        Optional<String> ticker;
        if (operation.getFigi() == null) {
            ticker = Optional.ofNullable(getSpecialTicker(operation));
        } else {
            ticker = Optional.ofNullable(stockRepository.getByFigi(operation.getFigi()))
                    .map(StockMetadata::getTicker);
        }
        return ticker;
    }

    private String getTickerFromTinkoff(Operation operation) {
        String ticker = null;
        if (StringUtils.hasText(operation.getFigi())) {

            try {
                var response = Optional.ofNullable(
                        tinkoffClient.getInstrumentInfoByFigi(operation.getFigi())
                                .getPayload());
                response.ifPresent(payload ->
                        ExecutorServiceUtils.execute(() ->
                                        stockRepository.save(StockMetadata.builder()
                                                .ticker(payload.getTicker())
                                                .figi(payload.getFigi())
                                                .instrumentType(payload.getType())
                                                .isin(payload.getIsin())
                                                .name(payload.getName())
                                                .build()),
                                Executors.newSingleThreadExecutor()));
                ticker = response.map(Instrument.Payload::getTicker).orElse(null);
            } catch (FeignException e) {
                log.error("Failed to get stock metadata, figi {}", operation.getFigi());
            }

        }
        if (ticker == null) {
            log.warn("Operation is not recognized {}", operation);
        }
        return ticker;
    }

    private String getSpecialTicker(Operation operation) {
        var function = SPECIAL_TICKERS.get(operation.getOperationType());
        if (function == null) {
            log.warn("Failed to find function for type {}", operation.getOperationType());
        }
        //received operation w/o figi & type = brokerCommission
        return function != null ? function.apply(operation) : SERVICE_COMMISSION_PROC.apply(operation);
    }
}
