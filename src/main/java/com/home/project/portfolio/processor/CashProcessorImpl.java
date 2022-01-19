package com.home.project.portfolio.processor;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.portfolio.Currencies;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * Class to populate portfolioDto by assets distribution
 */
@Component
@Log4j2
public class CashProcessorImpl implements AccountProcessor {

    private final TinkoffClient tinkoffClient;

    public CashProcessorImpl(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    @Override
    public void apply(String accountId, PortfolioDto portfolioDto) {

        log.info("Getting currencies for accountId {}", accountId);
        var currencies = tinkoffClient.getCurrencies(accountId);
        if (!currencies.getStatus().equalsIgnoreCase("ok")) {
            log.warn("Retrieved portfolio contains non ok status: {}", currencies.getStatus());
            return;
        }
        if (currencies.getPayload() == null) {
            log.warn("Retrieved null payload for portfolio, accountId {}", accountId);
            return;
        }
        Stream.of(currencies)
                .map(Currencies::getPayload)
                .peek(c -> log.info("Retrieved {} currencies", currencies.getPayload().getCurrencies().size()))
                .forEach(c -> c.getCurrencies()
                        .forEach(currency -> portfolioDto.getCash().put(currency.getCurrency(), PortfolioDto.CurrencyDto.builder()
                                .balance(currency.getBalance())
                                .build())));
    }
}
