package com.home.project.portfolio.client;

import com.home.project.portfolio.config.YahooFinanceFeignConfig;
import com.home.project.portfolio.model.yahoo.YahooFinanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author rlagay
 */
@FeignClient(name = "yahooFinanceClient", url = "${yahoo.finance.api.url}", configuration = YahooFinanceFeignConfig.class)
public interface YahooFinanceClient {

    @GetMapping("/{ticker}?modules=assetProfile")
    YahooFinanceResponse getCompanyOverview(@PathVariable(value = "ticker") String ticker);
}
