package com.home.project.portfolio.controller;

import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.response.AnalyticDto;
import com.home.project.portfolio.service.AnalyticService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/analytic", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnalyticController {

    private final AnalyticService analyticService;

    public AnalyticController(AnalyticService analyticService) {
        this.analyticService = analyticService;
    }

    @GetMapping
    public ResponseEntity getAnalyticFor(@RequestParam String accountId, @RequestParam(value = "period") String period) {
        var parsed = Period.parse(period);
        log.debug("Starting analysis for period {}", parsed.getInterval());
        AnalyticDto analyticDto;
        try {
            analyticDto = analyticService.analyzeAccount(accountId, parsed);
        } catch (FeignException e) {
            log.warn("Failed to retrieve information from tinkoff, exception {}, status {}", e.getCause(), e.status());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(e.getCause());
        } catch (Exception e) {
            log.error("Failed to analyze operations, exception {}", e.getCause().getClass(), e.getCause());
            return ResponseEntity.internalServerError().body(e.getCause());
        }
        return ResponseEntity.ok(analyticDto);
    }
}
