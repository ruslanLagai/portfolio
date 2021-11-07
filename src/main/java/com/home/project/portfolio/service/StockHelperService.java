package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.entity.StockMetadata;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.repository.StockRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.home.project.portfolio.utils.Constants.CURRENCY_STRING_MAP;
import static com.home.project.portfolio.utils.OperationGroups.COMMISSIONS;

/**
 * Service to help
 */
@Service
@Log4j2
public class StockHelperService {

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
        if (COMMISSIONS.contains(operation.getOperationType()) && operation.getFigi() == null) {
            ticker = Optional.ofNullable(getServiceCommissionTicker(operation));
        } else {
            ticker = Optional.ofNullable(stockRepository.getByFigi(operation.getFigi()))
                    .map(StockMetadata::getTicker);
        }
        return ticker;
    }

    private String getTickerFromTinkoff(Operation operation) {
        String ticker = null;
        if (StringUtils.hasText(operation.getFigi())) {
            ticker = Optional.ofNullable(tinkoffClient.getInstrumentInfoByFigi(operation.getFigi()))
                    .filter(instrument -> instrument.getPayload() != null)
                    .map(instrument -> instrument.getPayload().getTicker())
                    .orElse(null);
        }
        if (ticker == null) {
            log.warn("Operation is not recognized {}", operation);
        }
        return ticker;
    }

    private String getServiceCommissionTicker(Operation operation) {
        return CURRENCY_STRING_MAP.getOrDefault(operation.getCurrency(), null);
    }
}
