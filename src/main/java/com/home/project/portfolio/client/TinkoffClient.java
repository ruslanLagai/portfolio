package com.home.project.portfolio.client;

import com.home.project.portfolio.model.operations.Instrument;
import com.home.project.portfolio.model.operations.Operations;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client to get data from Tinkoff
 */
@FeignClient(name = "tinkoffClient", url = "${tinkoff.api.url}")
public interface TinkoffClient {

    @GetMapping("/operations")
    Operations getOperations(@RequestParam String from, @RequestParam String to, @RequestParam String brokerAccountId);

    @GetMapping("/operations")
    Operations getOperationsOnStock(@RequestParam String from, @RequestParam String to,
                              @RequestParam String figi, @RequestParam String brokerAccountId);

    @GetMapping("/market/search/by-figi")
    Instrument getInstrumentInfoByFigi(@RequestParam String figi);

}

