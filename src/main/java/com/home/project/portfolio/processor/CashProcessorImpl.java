package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.core.models.Positions;

import java.util.concurrent.CompletableFuture;

/**
 * Class to populate portfolioDto by assets distribution
 */
@Component
@Log4j2
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
