package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.response.PortfolioDto;
import ru.tinkoff.piapi.core.models.Positions;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AccountProcessor {

    void apply(CompletableFuture<Positions> positions, String accountId, PortfolioDto portfolioDto);
}
