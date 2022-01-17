package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.response.PortfolioDto;

@FunctionalInterface
public interface AccountProcessor {

    void apply(String accountId, PortfolioDto portfolioDto);
}
