package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.Currency;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.home.project.portfolio.utils.Constants.CURRENCY_FIGI_MAP;

/**
 * @author rlagay
 */
@Service
@Slf4j
public class CurrencyService {

    private final TinkoffClient tinkoffClient;

    public CurrencyService(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    @NotNull
    public Map<Currency, Double> getCurrencyPrices(Set<Currency> existedCurrencies) {
        Map<Currency, Double> currencyPrices = new HashMap<>();
        for (Currency currency : existedCurrencies) {
            if (currency.equals(Currency.RUB)) {
                continue;
            }

            log.info("Getting current price for currency {}", currency.name());

            var response = tinkoffClient.getCurrentPrice(CURRENCY_FIGI_MAP.get(currency), 1);
            if (!response.getStatus().equalsIgnoreCase("ok")
                    || response.getPayload() == null) {
                log.warn("Failed to get price for {}", currency.name());
                continue;
            }
            currencyPrices.put(currency, response.getPayload().getLastPrice());

            log.debug("Current price for {} is {}", currency.name(), response.getPayload().getLastPrice());
        }
        return currencyPrices;
    }
}
