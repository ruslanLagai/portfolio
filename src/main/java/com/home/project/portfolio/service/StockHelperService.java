package com.home.project.portfolio.service;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.entity.StockMetadata;
import com.home.project.portfolio.repository.StockRepository;
import com.home.project.portfolio.utils.Constants;
import com.home.project.portfolio.utils.ExecutorServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Service to help
 */
@Service
@Log4j2
public record StockHelperService(InstrumentsService instrumentsService,
                                 StockRepository stockRepository) {

    private static final Function<Operation, String> PAYMENT_IN_PROC = operation ->
        Currency.parse(operation.getCurrency()).equals(Currency.USD) ? Constants.PAY_IN_USD
            : Currency.parse(operation.getCurrency()).equals(Currency.RUB) ? Constants.PAY_IN_RUB
            : Currency.parse(operation.getCurrency()).equals(Currency.EUR) ? Constants.PAY_IN_EUR
            : Currency.parse(operation.getCurrency()).equals(Currency.CNY) ? Constants.PAY_IN_CNY
            : Currency.parse(operation.getCurrency()).equals(Currency.HKD) ? Constants.PAY_IN_KHD : null;
    private static final Function<Operation, String> PAYMENT_OUT_PROC = operation ->
        Currency.parse(operation.getCurrency()).equals(Currency.USD) ? Constants.PAY_OUT_USD
            : Currency.parse(operation.getCurrency()).equals(Currency.RUB) ? Constants.PAY_OUT_RUB
            : Currency.parse(operation.getCurrency()).equals(Currency.EUR) ? Constants.PAY_OUT_EUR
            : Currency.parse(operation.getCurrency()).equals(Currency.CNY) ? Constants.PAY_OUT_CNY
            : Currency.parse(operation.getCurrency()).equals(Currency.HKD) ? Constants.PAY_OUT_KHD : null;
    private static final Function<Operation, String> TAX_PROC = operation -> Constants.TAX_RUB;
    private static final Function<Operation, String> SERVICE_COMMISSION_PROC = operation ->
        Currency.parse(operation.getCurrency()).equals(Currency.RUB) ? Constants.SERVICE_COMMISSION_RUB
            : Currency.parse(operation.getCurrency()).equals(Currency.USD) ? Constants.SERVICE_COMMISSION_USD
            : null;

    private static final Map<OperationType, Function<Operation, String>> SPECIAL_TICKERS = Map.ofEntries(
        Map.entry(OperationType.OPERATION_TYPE_INPUT, PAYMENT_IN_PROC),
        Map.entry(OperationType.OPERATION_TYPE_INPUT_SWIFT, PAYMENT_IN_PROC),
        Map.entry(OperationType.OPERATION_TYPE_OUTPUT_SWIFT, PAYMENT_IN_PROC),
        Map.entry(OperationType.OPERATION_TYPE_OUTPUT, PAYMENT_OUT_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_CORRECTION, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_BENEFIT_TAX, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_CORRECTION_COUPON, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_BOND_TAX_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_CORRECTION_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_REPO, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_REPO_HOLD, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_REPO_REFUND, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_TAX_REPO_REFUND_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_DIVIDEND_TAX_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_BENEFIT_TAX_PROGRESSIVE, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_DIVIDEND_TAX, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_BOND_TAX, TAX_PROC),
        Map.entry(OperationType.OPERATION_TYPE_BROKER_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_SERVICE_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_MARGIN_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_CASH_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_OUT_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_SUCCESS_FEE, SERVICE_COMMISSION_PROC),
        Map.entry(OperationType.OPERATION_TYPE_ADVICE_FEE, SERVICE_COMMISSION_PROC)
    );

    public String findTicker(Operation operation) {
        return getTickerFromDb(operation)
            .orElseGet(() -> getTickerFromTinkoff(operation));
    }

    private Optional<String> getTickerFromDb(Operation operation) {
        Optional<String> ticker;
        if (!StringUtils.hasText(operation.getFigi())) {
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
                var response = Optional.of(instrumentsService.getInstrumentByFigiSync(operation.getFigi()));
                response.ifPresent(instrument -> {
                    if (!stockRepository.existsByTicker(instrument.getTicker())) {
                        ExecutorServiceUtils.execute(() ->
                                stockRepository.save(StockMetadata.builder()
                                    .ticker(instrument.getTicker())
                                    .figi(instrument.getFigi())
                                    .instrumentType(InstrumentType.parse(instrument.getInstrumentType()))
                                    .isin(instrument.getIsin())
                                    .name(instrument.getName())
                                    .build()),
                            Executors.newSingleThreadExecutor());
                    }
                });
                ticker = response.map(Instrument::getTicker).orElse(null);
            } catch (ApiRuntimeException e) {
                log.error("Failed to get stock metadata, figi {}", operation.getFigi(), e);
            } catch (NullPointerException e) {
                log.error("Figi {}", operation.getFigi());
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
