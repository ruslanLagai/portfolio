package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.repository.OperationRepository;
import com.home.project.portfolio.utils.ConversionUtils;
import com.home.project.portfolio.utils.ExecutorServiceUtils;
import feign.FeignException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.home.project.portfolio.utils.Constants.FORMATTER;

/**
 * Service to retrieve operations
 * Part operations are retrieved from DB, latest from Tinkoff
 */
@Service
@Log4j2
public class OperationsService {

    private final TinkoffClient tinkoffClient;
    private final OperationRepository operationRepository;
    private final StockHelperService stockHelperService;

    public OperationsService(TinkoffClient tinkoffClient,
                             OperationRepository operationRepository,
                             StockHelperService stockHelperService) {
        this.tinkoffClient = tinkoffClient;
        this.operationRepository = operationRepository;
        this.stockHelperService = stockHelperService;
    }

    public List<Operation> getLastOperations(String accountId, Period period) {
        log.info("Retrieving operations for account {}, period {}", accountId, period.name());
        List<Operation> operations = new ArrayList<>();
        ZonedDateTime lastDbOperation = null;
        var start = ZonedDateTime.from(ZonedDateTime.now().minus(period.getPeriodDuration()));
        var end = ZonedDateTime.now();
        var dbOperations = operationRepository
                .getByAccountIdAndDateBetweenOrderByDateDesc(accountId, start, end);
        if (!CollectionUtils.isEmpty(dbOperations)) {
            lastDbOperation = dbOperations.iterator().next().getDate();
            log.debug("Last operation from DB has time {}", end);
            operations.addAll(ConversionUtils.convertToRestOperations(dbOperations));
        }

        try {
            var newOperations = tinkoffClient.getOperations(
                    lastDbOperation != null ? lastDbOperation.format(FORMATTER) : start.format(FORMATTER),
                    end.format(FORMATTER), accountId);
            if (newOperations != null && newOperations.getStatus().equalsIgnoreCase("ok")
                    && !CollectionUtils.isEmpty(newOperations.getPayload().getOperations())) {
                var payload = newOperations.getPayload().getOperations();
                payload.forEach(operation -> {
                    var ticker = stockHelperService.findTicker(operation);
                    operation.setTicker(ticker);
                });
                log.debug("Retrieved new {} operations", payload.size());
                operations.addAll(payload);

                ConversionUtils.convertToDbOperations(payload, accountId)
                        .forEach(operationEntity -> ExecutorServiceUtils.execute(() ->
                                operationRepository.save(operationEntity), Executors.newSingleThreadExecutor()));
            }
        } catch (FeignException e) {
            log.error("Failed to retrieve latest operations for account {}, status code {}, \nException: {}",
                    accountId, e.status(), e.getMessage());
            log.error(e.getStackTrace());
        }

        log.info("Retrieved {} operations for period: {}. Starting from {}", operations.size(),
                period.getPeriodDuration(), start);

        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
