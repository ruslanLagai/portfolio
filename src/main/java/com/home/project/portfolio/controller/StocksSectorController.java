package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.portfolio.Sector;
import com.home.project.portfolio.model.response.SectorsDto;
import com.home.project.portfolio.service.PortfolioService;
import com.home.project.portfolio.service.StockSectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

import java.util.List;

/**
 * @author rlagay
 */
@RestController
@RequestMapping(value = "/sectors", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class StocksSectorController {

    private final StockSectorService stockSectorService;
    private final PortfolioService portfolioService;

    public StocksSectorController(StockSectorService stockSectorService,
                                  PortfolioService portfolioService) {
        this.stockSectorService = stockSectorService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<SectorsDto> getStockSectors(@RequestParam String accountId) {
        List<Sector> sectors;
        try {
            var positions = portfolioService.getPositionsForAccount(accountId);
            sectors = stockSectorService.getSectorData(positions);
        } catch (ApiRuntimeException e) {
            log.error("Failed to retrieve data from tinkoff, exception {}, status code {}", e.getMessage(), e.getCode());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Failed to process sectors distribution, exception {}", e, e.getCause());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(SectorsDto.builder().sectors(sectors).build());
    }
}
