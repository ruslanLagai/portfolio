package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.operations.Operation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public StockHelperService(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    //todo check in cache -> db -> tinkoff
    public String findTicker(Operation operation) {
        var ticker = getTickerFromTinkoff(operation);
        return ticker;
    }

    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public String getTickerFromTinkoff(Operation operation) {
        String ticker = null;
        if (StringUtils.hasText(operation.getFigi())) {
            ticker = Optional.ofNullable(tinkoffClient.getInstrumentInfoByFigi(operation.getFigi()))
                    .filter(instrument -> instrument.getPayload() != null)
                    .map(instrument -> instrument.getPayload().getTicker())
                    .orElse(null);
        } else if (COMMISSIONS.contains(operation.getOperationType()) && operation.getFigi() == null) {
            ticker = CURRENCY_STRING_MAP.getOrDefault(operation.getCurrency(), null);
        }
        if (ticker == null) {
            log.warn("Operation is not recognized {}", operation);
        }
        return ticker;
    }
}
