package com.home.project.portfolio.processor;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

/**
 * Class to populate portfolioDto by stocks
 */
@Component
@Log4j2
public class AccountProcessorImpl implements AccountProcessor {

    private final TinkoffClient tinkoffClient;

    public AccountProcessorImpl(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    @Override
    public void apply(Account account, PortfolioDto portfolioDto) {
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
                .peek(p -> {
                    log.info("Retrieved {} positions", portfolio.getPayload().getPositions().size());
                    portfolioDto.addPositions(account.getBrokerAccountType().name(), portfolio.getPayload().getPositions());
                })
                .forEach(p -> p.getPayload().getPositions()
                        .forEach(positions -> {
                            log.info("Getting prices for instruments, figi {}", positions.getFigi());
                            var overbook = tinkoffClient.getCurrentPrice(positions.getFigi(), 1);
                            log.debug("Received overbook for {}, overbook {}", positions.getFigi(),
                                    overbook.getPayload().toString());
                            portfolioDto.addPrice(positions.getFigi(), overbook.getPayload());
                        })
                );
    }
}
