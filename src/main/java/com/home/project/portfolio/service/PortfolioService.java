package com.home.project.portfolio.service;

import com.home.project.portfolio.mapper.AccountMapper;
import com.home.project.portfolio.mapper.PositionMapper;
import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.processor.AccountProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.PositionsResponse;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.UsersService;
import ru.tinkoff.piapi.core.models.FuturePosition;
import ru.tinkoff.piapi.core.models.Positions;
import ru.tinkoff.piapi.core.models.SecurityPosition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to get information about current stocks in portfolio
 */
@Component
@Slf4j
public record PortfolioService(UsersService usersService,
                               OperationsService operationsService,
                               InstrumentsService instrumentsService,
                               List<AccountProcessor> accountProcessors,
                               AccountMapper accountMapper,
                               PositionMapper positionMapper) {

    public List<Account> getAccounts() {
        return usersService.getAccountsSync().stream()
            .filter(account -> !AccountStatus.ACCOUNT_STATUS_CLOSED.equals(account.getStatus()))
            .map(accountMapper::mapAccount)
            .collect(Collectors.toList());
    }

    public PortfolioDto getPortfolio(String accountId) {
        var portfolioDto = new PortfolioDto();
        var positions = operationsService.getPositions(accountId)
            .exceptionally(throwable -> {
                log.error("Failed to get positions, accountId {}", accountId, throwable);
                return Positions.fromResponse(PositionsResponse.newBuilder().build());
            });
        // add stock & funds & bounds data
        accountProcessors
            .forEach(accountProcessor -> accountProcessor.apply(positions, accountId, portfolioDto));
        return portfolioDto;
    }

    public List<Position> getPositionsForAccount(String accountId) {
        log.info("Retrieving positions for {}", accountId);
        var positions = operationsService.getPositionsSync(accountId);
        return operationsService.getPortfolioSync(accountId).getPositions()
            .stream().map(position -> {
                var instrument = instrumentsService.getInstrumentByFigiSync(position.getFigi());
                var blocked = positions.getSecurities().stream()
                    .filter(share -> share.getFigi().equals(position.getFigi()))
                    .map(SecurityPosition::getBlocked)
                    .findFirst()
                    .orElseGet(() -> positions.getFutures().stream()
                        .filter(futurePosition -> futurePosition.getFigi().equals(position.getFigi()))
                        .findFirst()
                        .map(FuturePosition::getBlocked)
                        .orElse(null)
                    );
                return positionMapper.map(position, instrument, blocked);
            }).toList();
    }
}
