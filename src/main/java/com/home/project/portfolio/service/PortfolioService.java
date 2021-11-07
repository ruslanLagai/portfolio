package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.processor.AccountProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Class to get information about current stocks in portfolio
 */
@Component
@Log4j2
public class PortfolioService {

    private final TinkoffClient tinkoffClient;
    private final List<AccountProcessor> accountProcessors;

    public PortfolioService(TinkoffClient tinkoffClient,
                            List<AccountProcessor> accountProcessors) {
        this.tinkoffClient = tinkoffClient;
        this.accountProcessors = accountProcessors;
    }

    //todo put to DB
    public PortfolioDto getPortfolio() {
        var portfolioDto = new PortfolioDto();
        tinkoffClient.getUserAccounts().getPayload().getAccounts()
                .forEach(account -> accountProcessors
                        .forEach(accountProcessor -> accountProcessor.apply(account, portfolioDto)));
        return portfolioDto;
    }

    public List<Position> getPositionsForAccount(String accountId) {
        log.info("Retrieving positions for {}", accountId);
        var portfolio = tinkoffClient.getPortfolioForAccount(accountId);
        return portfolio.getPayload() == null || CollectionUtils.isEmpty(portfolio.getPayload().getPositions())
                ? Collections.emptyList() : portfolio.getPayload().getPositions();
    }

    public PortfolioDto getCurrentPrice(String figi, PortfolioDto portfolioDto) {
        var overbook = tinkoffClient.getCurrentPrice(figi, 1);
        portfolioDto.addPrice(figi, overbook.getPayload());
        return portfolioDto;
    }

}
