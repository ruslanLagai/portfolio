package com.home.project.portfolio.service;

import com.home.project.portfolio.model.analytic.AnalyticData;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.response.AnalyticDto;
import com.home.project.portfolio.processor.AnalyticProcessor;
import com.home.project.portfolio.utils.AnalyticUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to get financial results on stocks
 * revenue
 * taxes
 * commission
 * payments
 */
@Service
@Log4j2
public record AnalyticService(List<AnalyticProcessor> processors,
                              OperationsService operationsService,
                              PortfolioService portfolioService) {

    public AnalyticDto analyzeAccount(String accountId, LocalDate date) {
        List<AnalyticData> analyticDataList = new ArrayList<>();
        MultiValueMap<String, Operation> operationsByTicker = new LinkedMultiValueMap<>();
        log.info("Calculating financial results for account {}, from {}", accountId, date);

        var positions = portfolioService.getPositionsForAccount(accountId);

        var operations = operationsService.getLastOperations(accountId, date);
        operations.forEach(operation -> operationsByTicker.add(operation.getTicker(), operation));
        log.debug("Operations by ticker size {}", operationsByTicker.size());

        processors.forEach(analyticProcessor ->
                analyticDataList.addAll(
                        analyticProcessor.apply(operationsByTicker, positions, accountId)));

        return AnalyticUtils.mergeAnalyticData(analyticDataList);
    }
}
