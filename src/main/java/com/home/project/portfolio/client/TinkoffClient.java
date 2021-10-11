package com.home.project.portfolio.client;

import com.home.project.portfolio.config.TinkoffFeignConfig;
import com.home.project.portfolio.model.operations.Operations;
import com.home.project.portfolio.model.portfolio.Accounts;
import com.home.project.portfolio.model.portfolio.Portfolio;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Client to get data from Tinkoff
 */
@FeignClient(name = "tinkoffClient", url = "${tinkoff.api.url}", configuration = TinkoffFeignConfig.class)
public interface TinkoffClient {

    @GetMapping("/user/accounts")
    Accounts getUserAccounts();

    @GetMapping("/portfolio")
    Portfolio getPortfolioForAccount(@RequestParam String accountId);

    @GetMapping("/operations")
    Operations getOperations(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to, @RequestParam String brokerAccountId);

    @GetMapping("/operations")
    void getOperationsOnStock(@RequestParam LocalDateTime from, @RequestParam LocalDateTime to,
                              @RequestParam String figi, @RequestParam String brokerAccountId);


}

