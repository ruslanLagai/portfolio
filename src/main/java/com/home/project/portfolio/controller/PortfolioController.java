package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.response.PortfolioDto;
import com.home.project.portfolio.service.PortfolioService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/portfolio", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<PortfolioDto> getStocks(@RequestParam String accountId) {
        PortfolioDto portfolio;
        try {
            portfolio = portfolioService.getPortfolio(accountId);
        } catch (FeignException.Unauthorized e) {
            log.warn("Retrieved unauthorized exception from Tinkoff. Token is absent or invalid");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (FeignException e) {
            log.error("Failed to retrieve data from tinkoff, exception {}, status code {}", e.getMessage(), e.status());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            log.error("Failed to process portfolio, exception {}", e.getCause());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(portfolio);
    }
}
