package com.home.project.portfolio.client;

import com.home.project.portfolio.config.TinkoffFeignConfig;
import com.home.project.portfolio.model.operations.Instrument;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.operations.PriceResponse;
import com.home.project.portfolio.model.portfolio.Accounts;
import com.home.project.portfolio.model.portfolio.Currencies;
import com.home.project.portfolio.model.portfolio.Portfolio;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client to get data from Tinkoff
 */
@FeignClient(name = "tinkoffClient", url = "${tinkoff.api.url}", configuration = TinkoffFeignConfig.class)
public interface TinkoffClient {

    @GetMapping("/user/accounts")
    Accounts getUserAccounts();

    @GetMapping("/portfolio")
    Portfolio getPortfolioForAccount(@RequestParam String brokerAccountId);

    @GetMapping("/operations")
    Operations getOperations(@RequestParam String from, @RequestParam String to, @RequestParam String brokerAccountId);

    @GetMapping("/operations")
    Operations getOperationsOnStock(@RequestParam String from, @RequestParam String to,
                              @RequestParam String figi, @RequestParam String brokerAccountId);

    @GetMapping("/market/search/by-figi")
    Instrument getInstrumentInfoByFigi(@RequestParam String figi);

    @GetMapping("/market/orderbook")
    PriceResponse getCurrentPrice(@RequestParam String figi, @RequestParam int depth);

    @GetMapping("/portfolio/currencies")
    Currencies getCurrencies(@RequestParam String accountId);
}

