package com.home.project.portfolio.service;

import com.home.project.portfolio.exception.NoCurrencyException;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.utils.PriceUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.core.MarketDataService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.home.project.portfolio.utils.Constants.CURRENCY_FIGI_MAP;

/**
 * @author rlagay
 */
@Service
@Slf4j
public record CurrencyService(MarketDataService marketDataService) {

    @NotNull
    public Map<Currency, Double> getCurrencyPrices(Set<Currency> existedCurrencies) {
        Map<Currency, Double> currencyPrices = new HashMap<>();
        log.info("Getting current price for currencies");
        var response = marketDataService.getLastPricesSync(CURRENCY_FIGI_MAP.values());
        for (Currency currency : existedCurrencies) {
            if (currency.equals(Currency.RUB)) {
                continue;
            }

            var price = response.stream()
                    .filter(lastPrice -> lastPrice.getFigi().equals(CURRENCY_FIGI_MAP.get(currency)))
                    .map(LastPrice::getPrice)
                    .map(PriceUtils::toDoubleValue)
                    .findFirst()
                    .orElseGet(() -> {
                        log.error("Failed to find price for currency {}. Currency should be added", currency);
                        throw new NoCurrencyException();
                    });
            currencyPrices.put(currency, price);
        }
        return currencyPrices;
    }
}
