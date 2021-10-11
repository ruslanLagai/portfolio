package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.portfolio.Accounts;
import com.home.project.portfolio.model.response.OperationsDto;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class to get information about current stocks in portfolio
 */
@Component
@Log4j2
public class PortfolioService {

    private final TinkoffClient tinkoffClient;

    public PortfolioService(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    //todo put to DB
    public PortfolioDto getPortfolio() {
        var portfolioDto = new PortfolioDto();
        tinkoffClient.getUserAccounts().getPayload().getAccounts()
                .forEach(account -> populateStocksData(portfolioDto, account));
        return portfolioDto;
    }

    //todo first check in cache, then in DB, if no -> rest
    public OperationsDto getLastOperations(String accountId, Period period) {
        var operationsDto = new OperationsDto();
        var start = LocalDateTime.from(Instant.now().minus(period.getPeriodDuration()));
        var operations = tinkoffClient.getOperations(start, LocalDateTime.now(), accountId);
        return populateOperationsData(operationsDto, operations);
    }

    private OperationsDto populateOperationsData(OperationsDto operationsDto, Operations operations) {
        if (operations == null || operations.getPayload() == null ||
                !operations.getStatus().equalsIgnoreCase("ok")) {
            log.warn("Failed to retrieve operations");
            return operationsDto;
        }
        operations.getPayload().getOperations()
                .forEach(operation -> operationsDto.addOperationOnStock(operation.getTicker(), operation));
        operationsDto.sortOperationsByDate();
        return operationsDto;
    }

    private void populateStocksData(PortfolioDto portfolioDto, Account account) {
        log.info("Getting positions for accountId {}, accountType {}",
                account.getBrokerAccountId(), account.getBrokerAccountType().name());
        var portfolio = tinkoffClient.getPortfolioForAccount(account.getBrokerAccountId());
        if (!portfolio.getStatus().equalsIgnoreCase("ok")) {
            log.warn("Retrieved portfolio contains non ok status: {}", portfolio.getStatus());
        }
        if (portfolio.getPayload() == null) {
            log.warn("Retrieved null payload for portfolio, accountId {}", account.getBrokerAccountId());
        }
        Stream.of(portfolio)
                .filter(p -> p.getStatus().equalsIgnoreCase("ok"))
                .filter(p -> p.getPayload() != null)
                .forEach(p -> {
                    log.info("Retrieved {} positions", portfolio.getPayload().getPositions().size());
                    portfolioDto.addPositions(account, portfolio.getPayload().getPositions());
                });

    }

}
