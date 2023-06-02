package com.home.project.portfolio.processor;

import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.portfolio.AveragePositionItem;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.portfolio.Distribution;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.service.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.util.concurrent.AtomicDouble;
import ru.tinkoff.piapi.core.models.Positions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Constants.CURRENCY_FIGI_MAP;

/**
 * Class to populate portfolioDto by assets distribution
 */
@Component
@Slf4j
public class PortfolioDistributionProcessorImpl implements AccountProcessor {

    private final CurrencyService currencyService;

    public PortfolioDistributionProcessorImpl(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void apply(CompletableFuture<Positions> positions, String accountId, PortfolioDto portfolioDto) {

        Map<String, Currency> currencyByFigi = portfolioDto.getPositions().stream()
                .collect(Collectors.toMap(Position::getFigi, position -> position.getAveragePositionPrice().currency()));
        Map<String, Double> lotsByFigi = portfolioDto.getPositions().stream()
                .collect(Collectors.toMap(Position::getFigi, Position::getBalance));

        // collect currencies except RUB, it will have default value
        var existedCurrencies = portfolioDto.getCash().keySet().stream()
                .filter(currency -> !currency.equals(Currency.RUB))
                .collect(Collectors.toSet());
        portfolioDto.getPositions().stream()
                .map(Position::getAveragePositionPrice)
                .map(AveragePositionItem::currency)
                .filter(currency -> !currency.equals(Currency.RUB))
                .forEach(existedCurrencies::add);

        log.info("Collected currencies size {}", existedCurrencies.size());

        // collect prices
        Map<Currency, Double> currencyPrices = currencyService.getCurrencyPrices(existedCurrencies);

        // total assets
        var sum = new AtomicDouble();
        portfolioDto.getPrices().values()
                .forEach(overbook -> {
                    var result = overbook.getLastPrice() * lotsByFigi.get(overbook.getFigi())
                            * currencyPrices.getOrDefault(currencyByFigi.get(overbook.getFigi()), 1.0);
                    sum.addAndGet(result);
                });
        if (portfolioDto.getCash().get(Currency.RUB) != null) {
            sum.addAndGet(portfolioDto.getCash().get(Currency.RUB).getBalance());
        }

        var distribution = Distribution.builder()
                .assetsInRub(Precision.round(sum.get(), 2))
                .assetsInUsd(currencyPrices.get(Currency.USD) != null
                    ? Precision.round(sum.get() / currencyPrices.get(Currency.USD), 2) : 0)
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
            currencyDto.setCurrentPrice(currencyPrices.getOrDefault(currency, 1.0));
            portfolioDto.getPositions().stream()
                    .filter(position -> position.getInstrumentType().equals(InstrumentType.CURRENCY))
                    .filter(position -> position.getFigi().equals(figi))
                    .forEach(position -> currencyDto.setAveragePrice(position.getAveragePositionPrice().value()));
        });
    }

    private double calculateTotalInCash(PortfolioDto portfolioDto, Map<Currency, Double> currencyPrices) {
        double result = 0.0;
        for (Map.Entry<Currency, PortfolioDto.CurrencyDto> entry : portfolioDto.getCash().entrySet()) {
            result += entry.getValue().getBalance() * currencyPrices.getOrDefault(entry.getKey(), 1.0);
        }
        log.info("Calculated total in cash {}", result);
        return Precision.round(result, 2);
    }

    private double calculateTotalInType(PortfolioDto portfolioDto, Map<Currency, Double> currencyPrices,
                                          InstrumentType type) {
        AtomicDouble sum = new AtomicDouble();
        portfolioDto.getPositions().stream()
                .filter(position -> position.getInstrumentType().equals(type))
                .forEach(position -> {
                    var result = position.getBalance() * portfolioDto.getPrices().get(position.getFigi()).getLastPrice()
                            *  currencyPrices.getOrDefault(position.getAveragePositionPrice().currency(), 1.0);
                    sum.addAndGet(result);
                });

        log.info("Calculated total in {} {}", type, sum);
        return Precision.round(sum.get(), 2);
    }
}
