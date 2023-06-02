package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.core.models.Positions;

import java.util.concurrent.CompletableFuture;

/**
 * Class to populate portfolioDto by assets distribution
 */
@Component
@Slf4j
public class CashProcessorImpl implements AccountProcessor {

    @Override
    public void apply(CompletableFuture<Positions> completableFuture, String accountId, PortfolioDto portfolioDto) {
        log.info("Getting currencies for accountId {}", accountId);
        completableFuture.thenAccept(positions ->
                positions.getMoney().stream()
                    .peek(money -> log.info("Retrieved {} currencies", positions.getMoney().size()))
                    .forEach(money -> portfolioDto.getCash().put(
                        Currency.parse(money.getCurrency()),
                        PortfolioDto.CurrencyDto.builder()
                            .balance(money.getValue().doubleValue())
                            .build()))
        );
    }
}
