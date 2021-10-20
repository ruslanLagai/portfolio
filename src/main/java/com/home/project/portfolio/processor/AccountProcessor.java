package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.portfolio.Account;
import com.home.project.portfolio.model.response.PortfolioDto;

@FunctionalInterface
public interface AccountProcessor {

    void apply(Account account, PortfolioDto portfolioDto);
}
