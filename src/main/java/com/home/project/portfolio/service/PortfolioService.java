package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.response.OperationsDto;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.processor.AccountProcessor;
import com.home.project.portfolio.processor.OperationsProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.home.project.portfolio.utils.Constants.TINKOFF_API_DATE_TIME_FORMAT;

/**
 * Class to get information about current stocks in portfolio
 */
@Component
@Log4j2
public class PortfolioService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TINKOFF_API_DATE_TIME_FORMAT);
    private final TinkoffClient tinkoffClient;
    private final List<OperationsProcessor> operationsProcessors;
    private final List<AccountProcessor> accountProcessors;

    public PortfolioService(TinkoffClient tinkoffClient,
                            List<OperationsProcessor> operationsProcessors,
                            List<AccountProcessor> accountProcessors) {
        this.tinkoffClient = tinkoffClient;
        this.operationsProcessors = operationsProcessors;
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

    //todo first check in cache, then in DB, if no -> rest
    public OperationsDto getLastOperations(String accountId, Period period) {
        var operationsDto = new OperationsDto();
        var start = ZonedDateTime.from(ZonedDateTime.now().minus(period.getPeriodDuration())).format(FORMATTER);
        var operations = tinkoffClient.getOperations(start, ZonedDateTime.now().format(FORMATTER), accountId);

        log.info("Retrieved operations for period: {}. Starting from {}", period.getPeriodDuration(), start);

        if (operations == null || operations.getPayload() == null ||
                !operations.getStatus().equalsIgnoreCase("ok")) {
            log.warn("Failed to retrieve operations");
            return operationsDto;
        }

        log.info("Retrieved {} operations", operations.getPayload().getOperations().size());

        operationsProcessors
                .forEach(operationsProcessor -> operationsProcessor.apply(operations, operationsDto));
        operationsDto.sortOperationsByDate();
        return operationsDto;
    }

}
