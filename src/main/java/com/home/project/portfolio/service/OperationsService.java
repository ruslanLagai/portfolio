package com.home.project.portfolio.service;

import com.home.project.portfolio.client.TinkoffClient;
import com.home.project.portfolio.model.analytic.Period;
import com.home.project.portfolio.model.entity.OperationEntity;
import com.home.project.portfolio.model.operations.Operation;
import com.home.project.portfolio.model.operations.Operations;
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
import java.util.Optional;
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

        // find the last operation to use it with Tinkoff API
        if (!CollectionUtils.isEmpty(dbOperations)) {
            lastDbOperation = dbOperations.iterator().next().getDate();
            log.debug("Last operation from DB has time {}", end);
        }

        try {
            // query Tinkoff API, it treats start date as inclusive, i.e. [start ... end)
            var newOperations = tinkoffClient.getOperations(
                    lastDbOperation != null ? lastDbOperation.format(FORMATTER) : start.format(FORMATTER),
                    end.format(FORMATTER), accountId);

            // if the response contains some data, it may contain entries already stored in the database
            if (newOperations != null && newOperations.getStatus().equalsIgnoreCase("ok")
                    && !CollectionUtils.isEmpty(newOperations.getPayload().getOperations())) {

                var payload = newOperations.getPayload().getOperations();
                log.debug("Retrieved new {} operations", payload.size());

                // filter out the ones which we have in the DB already
                payload.stream().filter(operation -> {
                    Optional<OperationEntity> matchingObject = dbOperations.stream().
                            filter(o -> o.getOperationId() == operation.getId()).
                            findFirst();
                    return matchingObject.isEmpty();
                }).collect(Collectors.toSet()).forEach(operation -> {
                    var ticker = stockHelperService.findTicker(operation);
                    operation.setTicker(ticker);
                    operations.add(operation);
                });

                // store true new operations to the database
                ConversionUtils.convertToDbOperations(operations, accountId)
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

        // add the old list of operations from database operations for the period selected
        operations.addAll(ConversionUtils.convertToRestOperations(dbOperations));

        return operations.stream()
                .sorted(Comparator.comparing(Operation::getDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public List<Operation> getLastOperationsForStock(ZonedDateTime from, ZonedDateTime to, String figi, String accountId) {
        try {
            return Optional.ofNullable(tinkoffClient
                            .getOperationsOnStock(from.format(FORMATTER), to.format(FORMATTER), figi, accountId).getPayload())
                    .map(Operations.Payload::getOperations)
                    .orElseGet(() -> {
                        log.warn("Retrieved empty operations for figi {}, to {}, from {}", figi, to, from);
                        return List.of();
                    });
        } catch (FeignException e) {
            log.warn("Failed to retrieve latest operations for account {}, status code {}, \nException: {}",
                    accountId, e.status(), e.getMessage());
            return List.of();
        }

    }
}
