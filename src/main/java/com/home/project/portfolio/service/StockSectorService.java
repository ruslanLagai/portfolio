package com.home.project.portfolio.service;

import com.home.project.portfolio.client.YahooFinanceClient;
import com.home.project.portfolio.model.Currency;
import com.home.project.portfolio.model.InstrumentType;
import com.home.project.portfolio.model.entity.CompanyEntity;
import com.home.project.portfolio.model.portfolio.Position;
import com.home.project.portfolio.model.portfolio.Sector;
import com.home.project.portfolio.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rlagay
 */
@Slf4j
@Service
public class StockSectorService {

    private final YahooFinanceClient yahooFinanceClient;
    private final CompanyRepository companyRepository;
    private final CurrencyService currencyService;

    public StockSectorService(YahooFinanceClient yahooFinanceClient,
                              CompanyRepository companyRepository,
                              CurrencyService currencyService) {
        this.yahooFinanceClient = yahooFinanceClient;
        this.companyRepository = companyRepository;
        this.currencyService = currencyService;
    }

    public List<Sector> getSectorData(List<Position> positionList) {
        MultiValueMap<String, Position> tickerBySector = retrieveSector(positionList);
        List<Sector> sectors = new ArrayList<>();
        var currencyPrices = currencyService.getCurrencyPrices(positionList.stream()
                .map(position -> position.getAveragePositionPrice().getCurrency())
                .collect(Collectors.toSet()));

        var total = positionList.stream()
                .filter(position -> position.getInstrumentType().equals(InstrumentType.STOCK))
                .map(position -> calculateAsset(position, currencyPrices))
                .mapToDouble(Double::doubleValue)
                .sum();

        tickerBySector.forEach((sector, positions) -> {
            var totalForSector = positions.stream()
                    .map(position -> calculateAsset(position, currencyPrices))
                    .reduce(Double::sum)
                    .orElse(0.0);

            log.info("Total sum for sector {} is {}", sector, totalForSector);

            sectors.add(Sector.builder()
                    .sector(sector)
                    .sectorWeight(Precision.round(totalForSector / total * 100, 2))
                    .build());
        });
        return sectors;
    }

    private Double calculateAsset(Position position, Map<Currency, Double> currencyPrices) {
        var price = position.getAveragePositionPrice().getValue() + position.getExpectedYield().getValue() / position.getBalance();
        double currency = currencyPrices.getOrDefault(position.getAveragePositionPrice().getCurrency(), 1.0);
        log.debug("Current price for {} is {}. currency multiplier {}", position.getTicker(), price, currency);
        return price * currency * position.getBalance();
    }


    private MultiValueMap<String, Position> retrieveSector(List<Position> positions) {
        MultiValueMap<String, Position> tickerBySector = new LinkedMultiValueMap<>();
        positions.stream()
                .filter(position -> position.getInstrumentType().equals(InstrumentType.STOCK))
                .peek(position -> log.info("Retrieving sector for {}", position.getTicker()))
                .forEach(position -> companyRepository.findByTicker(position.getTicker())
                        .ifPresentOrElse(
                                companyEntity -> tickerBySector.add(companyEntity.getSector(), position),
                                () -> Optional.of(yahooFinanceClient.getCompanyOverview(position.getTicker()))
                                        .ifPresentOrElse(response -> {
                                                var overview = response.getQuoteSummary().getResult().iterator().next();
                                                tickerBySector.add(overview.getAssetProfile().getSector(), position);
                                                companyRepository.save(CompanyEntity.builder()
                                                        .country(overview.getAssetProfile().getCountry())
                                                        .industry(overview.getAssetProfile().getIndustry())
                                                        .name(position.getName())
                                                        .sector(overview.getAssetProfile().getSector())
                                                        .figi(position.getFigi())
                                                        .ticker(position.getTicker())
                                                        .build());
                                                },
                                                () -> log.error("Failed to retrieve company overview from yahoo for {}", position.getTicker()))
                        )
                );
        return tickerBySector;
    }

}
