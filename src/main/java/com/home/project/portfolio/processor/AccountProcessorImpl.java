package com.home.project.portfolio.processor;

import com.home.project.portfolio.aop.PortfolioAspect;
import com.home.project.portfolio.mapper.PositionMapper;
import com.home.project.portfolio.model.operations.Overbook;
import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.utils.PriceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.Positions;
import ru.tinkoff.piapi.core.models.SecurityPosition;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Class to populate portfolioDto by stocks
 */
@Component
@Slf4j
public class AccountProcessorImpl implements AccountProcessor {

    private final OperationsService operationsService;
    private final InstrumentsService instrumentsService;
    private final PositionMapper positionMapper;
    private final MarketDataService marketDataService;
    private final PortfolioAspect portfolioAspect;

    public AccountProcessorImpl(OperationsService operationsService,
                                InstrumentsService instrumentsService,
                                PositionMapper positionMapper,
                                MarketDataService marketDataService,
                                PortfolioAspect portfolioAspect) {
        this.operationsService = operationsService;
        this.instrumentsService = instrumentsService;
        this.positionMapper = positionMapper;
        this.marketDataService = marketDataService;
        this.portfolioAspect = portfolioAspect;
    }

    @Override
    public void apply(CompletableFuture<Positions> positions, String accountId, PortfolioDto portfolioDto) {
        positions.thenAccept(pos -> {
                var shares = pos.getSecurities();
                var portfolio = operationsService.getPortfolioSync(accountId);

                var figis = shares.stream().map(SecurityPosition::getFigi).toList();
                log.info("Getting prices for {} instruments", figis.size());
                var lastPrices = marketDataService.getLastPricesSync(figis).stream()
                    .collect(Collectors.toMap(LastPrice::getFigi, lastPrice -> {
                        var price = PriceUtils.toDoubleValue(lastPrice.getPrice());
                        if (price == 0.0) {
                            price = getPriceForFrozenItems(lastPrice.getFigi());
                        }
                        var overbook = new Overbook();
                        overbook.setLastPrice(price);
                        overbook.setFigi(lastPrice.getFigi());
                        return overbook;
                    }));

                shares.stream()
                    .map(share -> {
                        var instrument = instrumentsService.getInstrumentByFigiSync(share.getFigi());
                        var position = portfolio.getPositions().stream()
                            .filter(position1 -> position1.getFigi().equals(share.getFigi()))
                            .findFirst()
                            .orElse(Position.builder().build());
                        lastPrices.get(share.getFigi()).setTradeStatus(instrument.getTradingStatus());
                        return positionMapper.map(position, instrument, share.getBlocked());
                    })
                    .peek(position -> log.info("Mapped position {}", position))
                    .forEach(position -> portfolioDto.getPositions().add(position));
                portfolioDto.getPrices().putAll(lastPrices);
                portfolioAspect.getSemaphore().release();
        });
    }

    private double getPriceForFrozenItems(String figi) {
        var overbook = marketDataService.getOrderBookSync(figi, 1);
        return PriceUtils.toDoubleValue(overbook.getLastPrice());
    }
}
