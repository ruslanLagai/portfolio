package com.home.project.portfolio.processor;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.portfolio.AveragePositionItem;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.portfolio.Distribution;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.util.concurrent.AtomicDouble;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to populate portfolioDto by assets distribution
 */
@Component
@Log4j2
public class PortfolioDistributionProcessorImpl implements AccountProcessor {

    private static final Map<Currency, String> CURRENCY_FIGI_MAP = Map.of(
            Currency.USD, "BBG0013HGFT4",
            Currency.EUR, "BBG0013HJJ31"
    );

    private final TinkoffClient tinkoffClient;

    public PortfolioDistributionProcessorImpl(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    @Override
    public void apply(String accountId, PortfolioDto portfolioDto) {

        Map<String, Currency> currencyByFigi = portfolioDto.getPositions().stream()
                .collect(Collectors.toMap(Position::getFigi, position -> position.getAveragePositionPrice().getCurrency()));
        Map<String, Double> lotsByFigi = portfolioDto.getPositions().stream()
                .collect(Collectors.toMap(Position::getFigi, Position::getBalance));

        // collect currencies except RUB, it will have default value
        var existedCurrencies = portfolioDto.getCash().keySet().stream()
                .filter(currency -> !currency.equals(Currency.RUB))
                .collect(Collectors.toSet());
        portfolioDto.getPositions().stream()
                .map(Position::getAveragePositionPrice)
                .map(AveragePositionItem::getCurrency)
                .filter(currency -> !currency.equals(Currency.RUB))
                .forEach(existedCurrencies::add);

        log.info("Collected currencies size {}", existedCurrencies.size());

        // collect prices
        Map<Currency, Double> currencyPrices = new HashMap<>();
        for (Currency currency : existedCurrencies) {

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

        // total assets
        var sum = new AtomicDouble();
        portfolioDto.getPrices().values()
                .forEach(overbook -> {
                    var result = overbook.getLastPrice() * lotsByFigi.get(overbook.getFigi())
                            * currencyPrices.getOrDefault(currencyByFigi.get(overbook.getFigi()), 1.0);
                    sum.addAndGet(result);
                });
        sum.addAndGet(portfolioDto.getCash().get(Currency.RUB).getBalance());

        var distribution = Distribution.builder()
                .assetsInRub(Math.floor(sum.get() * 100) / 100)
                .assetsInUsd(Math.floor(sum.get() / currencyPrices.get(Currency.USD) * 100) / 100)
                .totalInCash(calculateTotalInCash(portfolioDto, currencyPrices))
                .totalInStocks(calculateTotalInType(portfolioDto, currencyPrices, InstrumentType.STOCK))
                .totalInBounds(calculateTotalInType(portfolioDto, currencyPrices, InstrumentType.BOND))
                .totalInFunds(calculateTotalInType(portfolioDto, currencyPrices, InstrumentType.ETF))
                .build();

        portfolioDto.setDistribution(distribution);

        populateCashData(portfolioDto, currencyPrices);
    }

    private void populateCashData(@NotNull PortfolioDto portfolioDto, Map<Currency, Double> currencyPrices) {
        portfolioDto.getCash().forEach((currency, currencyDto) -> {
            var figi = CURRENCY_FIGI_MAP.getOrDefault(currency, "default");
            currencyDto.setCurrentPrice(currencyPrices.getOrDefault(currency, 0.0));
            portfolioDto.getPositions().stream()
                    .filter(position -> position.getInstrumentType().equals(InstrumentType.CURRENCY))
                    .filter(position -> position.getFigi().equals(figi))
                    .forEach(position -> currencyDto.setAveragePrice(position.getAveragePositionPrice().getValue()));
        });
    }

    private double calculateTotalInCash(PortfolioDto portfolioDto, Map<Currency, Double> currencyPrices) {
        double result = 0.0;
        for (Map.Entry<Currency, PortfolioDto.CurrencyDto> entry : portfolioDto.getCash().entrySet()) {
            result += entry.getValue().getBalance() * currencyPrices.getOrDefault(entry.getKey(), 1.0);
        }
        log.info("Calculated total in cash {}", result);
        return Math.floor(result * 100) / 100;
    }

    private double calculateTotalInType(PortfolioDto portfolioDto, Map<Currency, Double> currencyPrices,
                                          InstrumentType type) {
        AtomicDouble sum = new AtomicDouble();
        portfolioDto.getPositions().stream()
                .filter(position -> position.getInstrumentType().equals(type))
                .forEach(position -> {
                    var result = position.getBalance() * portfolioDto.getPrices().get(position.getFigi()).getLastPrice()
                            *  currencyPrices.getOrDefault(position.getAveragePositionPrice().getCurrency(), 1.0);
                    sum.addAndGet(result);
                });

        log.info("Calculated total in {} {}", type, sum);
        return Math.floor(sum.get() * 100) / 100;
    }
}
