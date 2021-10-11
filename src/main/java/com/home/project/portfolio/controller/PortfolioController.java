package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.portfolio.Accounts;
import com.home.project.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "/portfolio")
public class PortfolioController {

    private PortfolioService portfolioService;

    ResponseEntity<Accounts> getStocks() {
        portfolioService.getPortfolio();
        return ResponseEntity.ok(null);
    }
}
